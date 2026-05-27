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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the orders endpoints.
 *
 * Seed data (loaded once from data.sql when the Spring context starts):
 *   ID 1 → north, QUEUE
 *   ID 2 → north, PREPARING
 *   ID 3 → center, READY
 *
 * @AfterEach resets those three rows back to their original statuses and
 * deletes any extra orders created during tests, keeping the DB clean for
 * the next test method.
 */
@SpringBootTest
class OrdersControllerTest {

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
        setStatus(1L, OrderState.QUEUE);
        setStatus(2L, OrderState.PREPARING);
        setStatus(3L, OrderState.READY);
        // Delete any orders created by tests (seed IDs are 1, 2, 3)
        repo.findAll().stream()
                .filter(o -> o.getId() > 3L)
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
    void getOrders_centerBar_returnsOneReadyOrder() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "center"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("ready")));
    }

    @Test
    void getOrders_southBar_returnsEmpty() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "south"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
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

        // Delivered order should no longer show up in the list
        mockMvc.perform(get("/orders").param("bar", "center"))
                .andExpect(jsonPath("$", hasSize(0)));
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

        // Verify the ID is numeric and > 3 (cleanup @AfterEach handles deletion)
        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        long numericId = Long.parseLong(id.replace("FB", ""));
        assert numericId > 3 : "Expected created order id > 3, got " + numericId;
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
    void createOrder_missingBar_returns400() throws Exception {
        String body = """
                {"items":[{"pid":"p1","q":1}],"total":8000}
                """;
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
