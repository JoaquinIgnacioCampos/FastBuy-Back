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
 * Read-only integration tests for the static data endpoints.
 *
 * Products: p1–p15 (Eclipse), p16–p23 (Cumbiero), p_test = 24 total
 * Bars: eclipse-north, eclipse-center, eclipse-south, cumbia-main, cumbia-vip = 5 total
 * Events visible: e1 (live), e2 (live), e3 (upcoming), e5 (far future)
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
    void getProducts_returns24Items() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(24)));
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
    void getProducts_containsEclipseItems() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(jsonPath("$[?(@.id == 'p1')].name", hasItem("Carlsberg 500ml")))
                .andExpect(jsonPath("$[?(@.id == 'p11')].name", hasItem("Chorizo a la parrilla")));
    }

    @Test
    void getProducts_containsCumbieroItems() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(jsonPath("$[?(@.id == 'p16')].name", hasItem("Fernet con Coca")))
                .andExpect(jsonPath("$[?(@.id == 'p21')].name", hasItem("Provoleta a la parrilla")));
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
    void getBars_returns5Items() throws Exception {
        mockMvc.perform(get("/bars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    void getBars_hasRequiredFields() throws Exception {
        mockMvc.perform(get("/bars"))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].label", notNullValue()))
                .andExpect(jsonPath("$[0].location", notNullValue()))
                .andExpect(jsonPath("$[0].eventId", notNullValue()));
    }

    @Test
    void getBars_containsBothEventBars() throws Exception {
        mockMvc.perform(get("/bars"))
                .andExpect(jsonPath("$[*].id", hasItem("eclipse-north")))
                .andExpect(jsonPath("$[*].id", hasItem("cumbia-main")));
    }

    // ── /events/{id}/bars ─────────────────────────────────────────────────────

    @Test
    void getEventBars_eclipse_returns3Bars() throws Exception {
        mockMvc.perform(get("/events/e1/bars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getEventBars_cumbiero_returns2Bars() throws Exception {
        mockMvc.perform(get("/events/e2/bars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ── /events/{id}/menu ─────────────────────────────────────────────────────

    @Test
    void getEventMenu_eclipse_returns16Items() throws Exception {
        // 15 Eclipse products + p_test = 16 distinct items across all 3 bars
        mockMvc.perform(get("/events/e1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(16)));
    }

    @Test
    void getEventMenu_cumbiero_returns9Items() throws Exception {
        // p16-p23 (8 Cumbiero items) + p_test = 9 distinct items across 2 bars
        mockMvc.perform(get("/events/e2/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(9)));
    }

    @Test
    void getEventMenu_noOverlapBetweenEvents() throws Exception {
        // Eclipse menu should not contain Cumbiero-exclusive items and vice versa
        mockMvc.perform(get("/events/e1/menu"))
                .andExpect(jsonPath("$[*].id", not(hasItem("p16"))))
                .andExpect(jsonPath("$[*].id", not(hasItem("p21"))));
        mockMvc.perform(get("/events/e2/menu"))
                .andExpect(jsonPath("$[*].id", not(hasItem("p1"))))
                .andExpect(jsonPath("$[*].id", not(hasItem("p14"))));
    }

    // ── /bars/{id}/menu ──────────────────────────────────────────────────────

    @Test
    void getBarMenu_southIncludesVipExclusives() throws Exception {
        mockMvc.perform(get("/bars/eclipse-south/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 'p14')].name", hasItem("Champagne Premium")))
                .andExpect(jsonPath("$[?(@.id == 'p15')].name", hasItem("Whisky Premium")));
    }

    @Test
    void getBarMenu_northExcludesVipExclusives() throws Exception {
        mockMvc.perform(get("/bars/eclipse-north/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem("p14"))))
                .andExpect(jsonPath("$[*].id", not(hasItem("p15"))));
    }

    @Test
    void getBarMenu_returnsSubsetOfProducts() throws Exception {
        mockMvc.perform(get("/bars/eclipse-center/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThan(24))));
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
    // Active/visible: e1 (live), e2 (live), e3 (upcoming), e5 (far future)

    @Test
    void getEvents_returns4VisibleEvents() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void getEvents_includesTwoLiveEvents() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(jsonPath("$[*].name", hasItem("Festival Eclipse")))
                .andExpect(jsonPath("$[*].name", hasItem("Festival Cumbiero")));
    }

    @Test
    void getEvents_excludesFinalizedBeyondGrace() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(jsonPath("$[*].name", not(hasItem("Lollapalooza Argentina 2025"))));
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
