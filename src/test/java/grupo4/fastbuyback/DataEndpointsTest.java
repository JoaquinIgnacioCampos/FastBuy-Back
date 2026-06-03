package grupo4.fastbuyback;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Read-only integration tests for the static data endpoints:
 * /products, /bars, /categories, /events.
 *
 * No @AfterEach cleanup needed — these tests never modify state.
 */
@SpringBootTest
class DataEndpointsTest {

    @Autowired private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ── /products ─────────────────────────────────────────────────────────────

    @Test
    void getProducts_returns15Items() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(15)));
    }

    @Test
    void getProducts_hasRequiredFields() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].category", notNullValue()))
                .andExpect(jsonPath("$[0].price", notNullValue()))
                .andExpect(jsonPath("$[0].stock", notNullValue()))
                .andExpect(jsonPath("$[0].image", notNullValue()))
                .andExpect(jsonPath("$[0].emoji", notNullValue()))
                .andExpect(jsonPath("$[0].subtitle", notNullValue()));
    }

    @Test
    void getProducts_containsKnownItems() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(jsonPath("$[?(@.id == 'p1')].name", hasItem("Carlsberg 500ml")))
                .andExpect(jsonPath("$[?(@.id == 'p11')].name", hasItem("Chorizo a la parrilla")));
    }

    @Test
    void getProducts_includesVipExclusives() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(jsonPath("$[?(@.id == 'p14')].name", hasItem("Champagne Premium")))
                .andExpect(jsonPath("$[?(@.id == 'p15')].name", hasItem("Whisky Premium")));
    }

    @Test
    void getProducts_pricesArePositive() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(jsonPath("$[*].price", everyItem(greaterThan(0.0))));
    }

    // ── /bars ─────────────────────────────────────────────────────────────────

    @Test
    void getBars_returns3Items() throws Exception {
        mockMvc.perform(get("/bars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getBars_hasRequiredFields() throws Exception {
        mockMvc.perform(get("/bars"))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].label", notNullValue()))
                .andExpect(jsonPath("$[0].location", notNullValue()));
    }

    @Test
    void getBars_containsKnownBars() throws Exception {
        mockMvc.perform(get("/bars"))
                .andExpect(jsonPath("$[?(@.id == 'north')].label", hasItem("Barra Norte")))
                .andExpect(jsonPath("$[?(@.id == 'south')].label", hasItem("Barra Sur")));
    }

    // ── /bars/{id}/menu ──────────────────────────────────────────────────────

    @Test
    void getBarMenu_southIncludesVipExclusives() throws Exception {
        // South is the VIP bar; it should carry the premium-only champagne/whisky.
        mockMvc.perform(get("/bars/south/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 'p14')].name", hasItem("Champagne Premium")))
                .andExpect(jsonPath("$[?(@.id == 'p15')].name", hasItem("Whisky Premium")));
    }

    @Test
    void getBarMenu_northExcludesVipExclusives() throws Exception {
        mockMvc.perform(get("/bars/north/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem("p14"))))
                .andExpect(jsonPath("$[*].id", not(hasItem("p15"))));
    }

    @Test
    void getBarMenu_returnsSubsetOfProducts() throws Exception {
        // Each bar's menu must be a strict subset of /products
        mockMvc.perform(get("/bars/center/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThan(15))));
    }

    @Test
    void getBarMenu_unknownBar_returns404() throws Exception {
        mockMvc.perform(get("/bars/unknown/menu"))
                .andExpect(status().isNotFound());
    }

    // ── /categories ───────────────────────────────────────────────────────────

    @Test
    void getCategories_returns4Items() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void getCategories_hasEmojiField() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(jsonPath("$[0].emoji", notNullValue()))
                .andExpect(jsonPath("$[*].emoji", everyItem(not(emptyString()))));
    }

    @Test
    void getCategories_containsAllExpectedIds() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(jsonPath("$[*].id", containsInAnyOrder("drink", "snack", "food", "all")));
    }

    // ── /events ───────────────────────────────────────────────────────────────
    // Seeded events (relative to NOW): e1 active, e2 upcoming, e3 just finished
    // (within 6h grace), e4 finished 60 days ago (filtered out), e5 far future.
    // GET /events filters out events whose endsAt < NOW - 6h, ordered by startsAt.

    @Test
    void getEvents_returnsActiveAndUpcoming() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void getEvents_includesMultipleLiveEvents() throws Exception {
        // e1, e1b, e1c all overlap "now" — entry screen needs more than one
        // live option for the user to choose from.
        mockMvc.perform(get("/events"))
                .andExpect(jsonPath("$[*].name", hasItem("Festival Eclipse")))
                .andExpect(jsonPath("$[*].name", hasItem("Festival Cumbiero")))
                .andExpect(jsonPath("$[*].name", hasItem("Trasnoche Indie")));
    }

    @Test
    void getEvents_excludesFinalizedBeyondGrace() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(jsonPath("$[*].name", not(hasItem("Lollapalooza Argentina 2025"))))
                .andExpect(jsonPath("$[*].name", hasItem("Festival Eclipse")))
                .andExpect(jsonPath("$[*].name", hasItem("Lollapalooza Argentina 2027")));
    }

    @Test
    void getEvents_includesRecentlyFinishedWithinGrace() throws Exception {
        // Quilmes Rock ended 1h ago (< 6h grace) and should still appear
        mockMvc.perform(get("/events"))
                .andExpect(jsonPath("$[*].name", hasItem("Quilmes Rock")));
    }

    @Test
    void getEvents_hasRequiredFields() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].venue", notNullValue()))
                .andExpect(jsonPath("$[0].hours", notNullValue()))
                .andExpect(jsonPath("$[0].startsAt", notNullValue()))
                .andExpect(jsonPath("$[0].endsAt", notNullValue()));
    }
}
