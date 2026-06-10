package grupo4.fastbuyback;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * /payments/preference tests. The Spring context boots with
 * mercadopago.access-token blank (env var unset in test runs), so every test
 * exercises the simulated-fallback path — no real MP calls are made.
 *
 * The endpoint now accepts { items, total, eventId } directly; no order is
 * created before payment confirmation.
 */
@SpringBootTest
class PaymentsControllerTest {

    @Autowired private WebApplicationContext wac;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void createPreference_validItems_returnsSimulatedInitPoint() throws Exception {
        String body = """
                {"items":[{"pid":"p1","q":2}],"total":16000,"eventId":"e1"}
                """;
        mockMvc.perform(post("/payments/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.simulated", is(true)))
                .andExpect(jsonPath("$.initPoint", containsString("status=approved")));
    }

    @Test
    void createPreference_withoutEventId_alsoWorks() throws Exception {
        String body = """
                {"items":[{"pid":"p1","q":1}],"total":8000}
                """;
        mockMvc.perform(post("/payments/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.simulated", is(true)));
    }

    @Test
    void createPreference_emptyItems_returns400() throws Exception {
        String body = """
                {"items":[],"total":0}
                """;
        mockMvc.perform(post("/payments/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPreference_missingItems_returns400() throws Exception {
        String body = """
                {"total":8000}
                """;
        mockMvc.perform(post("/payments/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
