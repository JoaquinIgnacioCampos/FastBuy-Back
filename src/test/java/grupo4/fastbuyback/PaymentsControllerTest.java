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
    void createPreference_existingOrder_returnsSimulatedInitPoint() throws Exception {
        String body = """
                {"orderId":"FB1"}
                """;
        mockMvc.perform(post("/payments/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.simulated", is(true)))
                .andExpect(jsonPath("$.initPoint",
                        allOf(containsString("status=approved"),
                              containsString("external_reference=FB1"))));
    }

    @Test
    void createPreference_unprefixedOrderId_alsoWorks() throws Exception {
        // Frontend always sends "FBn" but the endpoint must tolerate both forms.
        String body = """
                {"orderId":"1"}
                """;
        mockMvc.perform(post("/payments/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initPoint", containsString("external_reference=1")));
    }

    @Test
    void createPreference_unknownOrder_returns404() throws Exception {
        String body = """
                {"orderId":"FB9999"}
                """;
        mockMvc.perform(post("/payments/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPreference_missingOrderId_returns400() throws Exception {
        String body = """
                {}
                """;
        mockMvc.perform(post("/payments/preference")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
