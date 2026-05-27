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
    void getProducts_returns13Items() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(13)));
    }

    @Test
    void getProducts_hasRequiredFields() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].name", notNullValue()))
                .andExpect(jsonPath("$[0].category", notNullValue()))
                .andExpect(jsonPath("$[0].price", notNullValue()))
                .andExpect(jsonPath("$[0].stock", notNullValue()));
    }

    @Test
    void getProducts_containsKnownItems() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(jsonPath("$[?(@.id == 'p1')].name", hasItem("Carlsberg 500ml")))
                .andExpect(jsonPath("$[?(@.id == 'p11')].name", hasItem("Chorizo a la parrilla")));
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

    @Test
    void getEvents_returns1Item() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getEvents_hasCorrectName() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(jsonPath("$[0].name", is("Lollapalooza Argentina 2025")));
    }

    @Test
    void getEvents_hasRequiredFields() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(jsonPath("$[0].id", notNullValue()))
                .andExpect(jsonPath("$[0].venue", notNullValue()))
                .andExpect(jsonPath("$[0].hours", notNullValue()));
    }
}
