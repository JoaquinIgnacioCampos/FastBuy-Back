package grupo4.fastbuyback;

import com.jayway.jsonpath.JsonPath;
import grupo4.fastbuyback.Entities.OrderState;
import grupo4.fastbuyback.Repositories.OrdersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the orders endpoints.
 *
 * Seed data (loaded once from data.sql when the Spring context starts):
 *   ID 1 → north,  QUEUE
 *   ID 2 → north,  PREPARING
 *   ID 3 → center, READY
 *   ID 4 → center, QUEUE
 *   ID 5 → center, PREPARING
 *   ID 6 → south,  QUEUE
 *   ID 7 → south,  PREPARING
 *   ID 8 → south,  READY
 *
 * @AfterEach restores those rows to their original statuses and deletes any
 * extra orders created during tests, keeping the DB clean for the next test.
 */
@SpringBootTest
class OrdersControllerTest {

    private static final Map<Long, OrderState> SEED_STATES = Map.of(
            1L, OrderState.QUEUE,
            2L, OrderState.PREPARING,
            3L, OrderState.READY,
            4L, OrderState.QUEUE,
            5L, OrderState.PREPARING,
            6L, OrderState.QUEUE,
            7L, OrderState.PREPARING,
            8L, OrderState.READY
    );

    @Autowired private WebApplicationContext wac;
    @Autowired private OrdersRepository repo;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ── Cleanup ──────────────────────────────────────────────────────────────

    @AfterEach
    void cleanup() {
        SEED_STATES.forEach(this::setStatus);
        repo.findAll().stream()
                .filter(o -> !SEED_STATES.containsKey(o.getId()))
                .forEach(repo::delete);
    }

    private void setStatus(Long id, OrderState state) {
        repo.findById(id).ifPresent(o -> {
            o.setStatus(state);
            repo.save(o);
        });
    }

    // ── GET /orders ───────────────────────────────────────────────────────────

    @Test
    void getOrders_northBar_returnsTwoActiveOrders() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "north"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getOrders_centerBar_returnsThreeActiveOrders() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "center"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].status", hasItem("ready")));
    }

    @Test
    void getOrders_southBar_returnsThreeActiveOrders() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "south"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    // ── JSON shape ────────────────────────────────────────────────────────────

    @Test
    void responseShape_idHasFbPrefix() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "north"))
                .andExpect(jsonPath("$[0].id", startsWith("FB")));
    }

    @Test
    void responseShape_itemsIsJsonArray() throws Exception {
        // items must be a JSON array, NOT a raw string like "[{\"pid\":\"p1\",\"q\":2}]"
        mockMvc.perform(get("/orders").param("bar", "north"))
                .andExpect(jsonPath("$[0].items", isA(List.class)))
                .andExpect(jsonPath("$[0].items[0].pid", notNullValue()))
                .andExpect(jsonPath("$[0].items[0].q", notNullValue()));
    }

    @Test
    void responseShape_statusIsLowercase() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "north"))
                .andExpect(jsonPath("$[0].status", matchesPattern("[a-z]+")))
                .andExpect(jsonPath("$[1].status", matchesPattern("[a-z]+")));
    }

    @Test
    void responseShape_allRequiredFieldsPresent() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "north"))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].total", notNullValue()))
                .andExpect(jsonPath("$[0].items", notNullValue()))
                .andExpect(jsonPath("$[0].status", notNullValue()))
                .andExpect(jsonPath("$[0].bar", notNullValue()))
                .andExpect(jsonPath("$[0].time", notNullValue()));
    }

    // ── Delivered orders are filtered out ────────────────────────────────────

    @Test
    void getOrders_deliveredOrdersAreFiltered() throws Exception {
        // Bring order 1 all the way to DELIVERED: QUEUE -> PREPARING -> READY -> DELIVERED
        mockMvc.perform(post("/orders/1/advance"));
        mockMvc.perform(post("/orders/1/advance"));
        mockMvc.perform(post("/orders/1/deliver"));

        mockMvc.perform(get("/orders").param("bar", "north"))
                .andExpect(jsonPath("$[*].id", not(hasItem("FB1"))));
    }

    // ── POST /orders/{id}/advance ─────────────────────────────────────────────

    @Test
    void advanceOrder_queueToPreparing() throws Exception {
        mockMvc.perform(post("/orders/1/advance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("FB1")))
                .andExpect(jsonPath("$.status", is("preparing")));
    }

    @Test
    void advanceOrder_preparingToReady() throws Exception {
        mockMvc.perform(post("/orders/2/advance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("FB2")))
                .andExpect(jsonPath("$.status", is("ready")));
    }

    @Test
    void advanceOrder_fromReady_returns422() throws Exception {
        // Order 3 is already READY — cannot advance further
        mockMvc.perform(post("/orders/3/advance"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @Test
    void advanceOrder_unknownId_returns404() throws Exception {
        mockMvc.perform(post("/orders/9999/advance"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    @Test
    void advanceOrder_acceptsFbPrefixedId() throws Exception {
        // Frontend sends IDs in "FB1" format; the controller strips the prefix
        mockMvc.perform(post("/orders/FB1/advance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("FB1")))
                .andExpect(jsonPath("$.status", is("preparing")));
    }

    // ── POST /orders/{id}/deliver ─────────────────────────────────────────────

    @Test
    void deliverOrder_readyToDelivered() throws Exception {
        mockMvc.perform(post("/orders/3/deliver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("delivered")));

        // Delivered order should no longer show up in the list — center has 3 seeds,
        // after delivering order 3 the remaining two (queue + preparing) stay visible.
        mockMvc.perform(get("/orders").param("bar", "center"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].id", not(hasItem("FB3"))));
    }

    @Test
    void deliverOrder_notReady_returns422() throws Exception {
        // Order 1 is QUEUE — must be READY to deliver
        mockMvc.perform(post("/orders/1/deliver"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    // ── POST /orders (create) ─────────────────────────────────────────────────

    @Test
    void createOrder_returns201WithQueueStatus() throws Exception {
        String body = """
                {"items":[{"pid":"p1","q":2},{"pid":"p8","q":1}],"bar":"south","total":21000}
                """;

        MvcResult result = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("queue")))
                .andExpect(jsonPath("$.bar", is("south")))
                .andExpect(jsonPath("$.total", is(21000.0)))
                .andExpect(jsonPath("$.id", startsWith("FB")))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andReturn();

        // Verify the ID is numeric and > 8 (8 seed rows; cleanup @AfterEach handles deletion)
        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        long numericId = Long.parseLong(id.replace("FB", ""));
        assert numericId > 8 : "Expected created order id > 8, got " + numericId;
    }

    @Test
    void createOrder_newOrderAppearsInList() throws Exception {
        String body = """
                {"items":[{"pid":"p6","q":1}],"bar":"north","total":3000}
                """;
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        // Now north should have 3 active orders (2 seed + 1 new)
        mockMvc.perform(get("/orders").param("bar", "north"))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void createOrder_emptyItems_returns400() throws Exception {
        String body = """
                {"items":[],"bar":"south","total":0}
                """;
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_omitsBar_serverAssignsLeastLoaded() throws Exception {
        // Bar is now optional — server picks the least-loaded bar that serves
        // every item. p1 is carried by 'north' (2 active orders, ~3 drinks) and
        // 'center' (3 active orders, ~3 drinks). The assignment must land on
        // a bar that actually carries p1.
        String body = """
                {"items":[{"pid":"p1","q":1}],"total":8000}
                """;
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bar", anyOf(is("north"), is("center"))))
                .andExpect(jsonPath("$.status", is("queue")));
    }

    @Test
    void createOrder_vipItem_routesToSouth() throws Exception {
        // Champagne (p14) is a south-only product, so assignment must pick south.
        String body = """
                {"items":[{"pid":"p14","q":1}],"total":18000}
                """;
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bar", is("south")));
    }

    @Test
    void createOrder_mixedCart_fallsBackToBestMatchBar() throws Exception {
        // p7 (Sprite) is only at north; p14 (Champagne) is only at south.
        // No single bar carries both. With the lenient fallback, the order still
        // gets created against the bar that serves the most items (a tiebreak
        // between north and south, both with 1 match — least-loaded wins).
        String body = """
                {"items":[{"pid":"p7","q":1},{"pid":"p14","q":1}],"total":21000}
                """;
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("queue")))
                .andExpect(jsonPath("$.bar", anyOf(is("north"), is("south"))));
    }

    @Test
    void createOrder_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
