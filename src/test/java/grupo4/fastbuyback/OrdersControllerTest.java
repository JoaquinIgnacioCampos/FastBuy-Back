package grupo4.fastbuyback;

import com.jayway.jsonpath.JsonPath;
import grupo4.fastbuyback.Entities.OrderState;
import grupo4.fastbuyback.Repositories.OrdersRepository;

import java.time.LocalDateTime;
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
 * Seed data (from data.sql):
 *   ID 1 → eclipse-north,  QUEUE
 *   ID 2 → eclipse-north,  PREPARING
 *   ID 3 → eclipse-center, READY
 *   ID 4 → eclipse-center, QUEUE
 *   ID 5 → eclipse-south,  QUEUE
 *   ID 6 → eclipse-south,  PREPARING
 *   ID 7 → cumbia-main,    QUEUE
 *   ID 8 → cumbia-vip,     PREPARING
 */
@SpringBootTest
class OrdersControllerTest {

    private static final Map<Long, OrderState> SEED_STATES = Map.of(
            1L, OrderState.QUEUE,
            2L, OrderState.PREPARING,
            3L, OrderState.READY,
            4L, OrderState.QUEUE,
            5L, OrderState.QUEUE,
            6L, OrderState.PREPARING,
            7L, OrderState.QUEUE,
            8L, OrderState.PREPARING
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
            o.setClaimedBy(null);
            o.setClaimedAt(null);
            repo.save(o);
        });
    }

    // ── GET /orders ───────────────────────────────────────────────────────────

    @Test
    void getOrders_northBar_returnsTwoActiveOrders() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "eclipse-north"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getOrders_centerBar_returnsTwoActiveOrders() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "eclipse-center"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].status", hasItem("ready")));
    }

    @Test
    void getOrders_southBar_returnsTwoActiveOrders() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "eclipse-south"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ── JSON shape ────────────────────────────────────────────────────────────

    @Test
    void responseShape_idHasFbPrefix() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "eclipse-north"))
                .andExpect(jsonPath("$[0].id", startsWith("FB")));
    }

    @Test
    void responseShape_itemsIsJsonArray() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "eclipse-north"))
                .andExpect(jsonPath("$[0].items", isA(List.class)))
                .andExpect(jsonPath("$[0].items[0].pid", notNullValue()))
                .andExpect(jsonPath("$[0].items[0].q", notNullValue()));
    }

    @Test
    void responseShape_statusIsLowercase() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "eclipse-north"))
                .andExpect(jsonPath("$[0].status", matchesPattern("[a-z]+")))
                .andExpect(jsonPath("$[1].status", matchesPattern("[a-z]+")));
    }

    @Test
    void responseShape_allRequiredFieldsPresent() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "eclipse-north"))
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
        // Advance order 1 to READY, then deliver it
        mockMvc.perform(post("/orders/1/advance"));
        mockMvc.perform(post("/orders/1/advance"));
        mockMvc.perform(post("/orders/1/deliver"));

        mockMvc.perform(get("/orders").param("bar", "eclipse-north"))
                .andExpect(jsonPath("$[*].id", not(hasItem("FB1"))));
    }

    @Test
    void getOrders_deliveredStatus_returnsDeliveredOrders() throws Exception {
        // Deliver order 3 (which is READY)
        mockMvc.perform(post("/orders/3/deliver")).andExpect(status().isOk());

        mockMvc.perform(get("/orders").param("bar", "eclipse-center").param("status", "delivered"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("FB3")));
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

        // center now has only 1 active order (the QUEUE one)
        mockMvc.perform(get("/orders").param("bar", "eclipse-center"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].id", not(hasItem("FB3"))));
    }

    @Test
    void deliverOrder_notReady_returns422() throws Exception {
        mockMvc.perform(post("/orders/1/deliver"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error", notNullValue()));
    }

    // ── POST /orders (create) ─────────────────────────────────────────────────

    @Test
    void createOrder_returns201WithQueueStatus() throws Exception {
        String body = """
                {"items":[{"pid":"p1","q":2},{"pid":"p8","q":1}],"bar":"eclipse-south","total":21000}
                """;

        MvcResult result = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("queue")))
                .andExpect(jsonPath("$.bar", is("eclipse-south")))
                .andExpect(jsonPath("$.total", is(21000.0)))
                .andExpect(jsonPath("$.id", startsWith("FB")))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andReturn();

        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        long numericId = Long.parseLong(id.replace("FB", ""));
        assert numericId > 8 : "Expected created order id > 8, got " + numericId;
    }

    @Test
    void createOrder_newOrderAppearsInList() throws Exception {
        String body = """
                {"items":[{"pid":"p6","q":1}],"bar":"eclipse-north","total":3000}
                """;
        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        // eclipse-north now has 3 active orders (2 seed + 1 new)
        mockMvc.perform(get("/orders").param("bar", "eclipse-north"))
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void createOrder_emptyItems_returns400() throws Exception {
        String body = """
                {"items":[],"bar":"eclipse-south","total":0}
                """;
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_omitsBar_serverAssignsLeastLoaded() throws Exception {
        // p1 is at eclipse-north and eclipse-center (both in event e1).
        // Without eventId, all bars are candidates. Assignment must land on one
        // of the bars that carries p1.
        String body = """
                {"items":[{"pid":"p1","q":1}],"total":8000}
                """;
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bar", anyOf(is("eclipse-north"), is("eclipse-center"))))
                .andExpect(jsonPath("$.status", is("queue")));
    }

    @Test
    void createOrder_vipItem_routesToSouth() throws Exception {
        // Champagne (p14) is eclipse-south only.
        String body = """
                {"items":[{"pid":"p14","q":1}],"total":18000,"eventId":"e1"}
                """;
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bar", is("eclipse-south")));
    }

    @Test
    void createOrder_mixedCart_fallsBackToBestMatchBar() throws Exception {
        // p7 (Sprite) is only at eclipse-north; p14 (Champagne) is only at eclipse-south.
        // No single bar carries both. Lenient fallback assigns best match.
        String body = """
                {"items":[{"pid":"p7","q":1},{"pid":"p14","q":1}],"total":21000,"eventId":"e1"}
                """;
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("queue")))
                .andExpect(jsonPath("$.bar", anyOf(is("eclipse-north"), is("eclipse-south"))));
    }

    @Test
    void createOrder_missingBody_returns400() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ── Bartender locking ─────────────────────────────────────────────────────

    @Test
    void advanceOrder_setsClaimForBartender() throws Exception {
        String body = """
                {"bartenderId":"eclipse-north"}
                """;
        mockMvc.perform(post("/orders/1/advance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("preparing")))
                .andExpect(jsonPath("$.claimedBy", is("eclipse-north")));
    }

    @Test
    void getOrders_expiredLock_resetsToQueue() throws Exception {
        // Simulate a bartender that claimed order 2 three minutes ago and disconnected
        repo.findById(2L).ifPresent(o -> {
            o.setClaimedBy("eclipse-north");
            o.setClaimedAt(LocalDateTime.now().minusMinutes(3));
            repo.save(o);
        });

        mockMvc.perform(get("/orders").param("bar", "eclipse-north"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='FB2')].status", hasItem("queue")))
                .andExpect(jsonPath("$[?(@.id=='FB2')].claimedBy", hasItem(nullValue())));
    }

    @Test
    void releaseOrder_returnsToQueue() throws Exception {
        // Claim order 2 (already PREPARING in seed)
        repo.findById(2L).ifPresent(o -> {
            o.setClaimedBy("eclipse-north");
            o.setClaimedAt(LocalDateTime.now());
            repo.save(o);
        });

        mockMvc.perform(post("/orders/2/release"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("queue")))
                .andExpect(jsonPath("$.claimedBy", nullValue()));
    }
}
