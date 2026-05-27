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

import java.util.Base64;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies that SecurityConfig is correctly configured:
 *   - All endpoints are accessible without authentication (permitAll)
 *   - CSRF protection is disabled (POST requests work without a token)
 *   - Invalid credentials are ignored, not rejected
 *     (HTTP Basic is not in our custom filter chain, so the Authorization
 *      header is simply passed through without triggering a 401 challenge)
 *
 * No state is modified, so no cleanup is needed.
 */
@SpringBootTest
class SecurityTest {

    @Autowired private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    // ── No authentication required ────────────────────────────────────────────

    @Test
    void ordersGetEndpoint_withoutAuth_isAccessible() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "north"))
                .andExpect(status().isOk());
    }

    @Test
    void productsEndpoint_withoutAuth_isAccessible() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    void barsEndpoint_withoutAuth_isAccessible() throws Exception {
        mockMvc.perform(get("/bars"))
                .andExpect(status().isOk());
    }

    @Test
    void categoriesEndpoint_withoutAuth_isAccessible() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void eventsEndpoint_withoutAuth_isAccessible() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk());
    }

    // ── Wrong credentials are not rejected ────────────────────────────────────
    // HTTP Basic is not registered in our custom SecurityFilterChain, so the
    // Authorization header is ignored rather than triggering a 401 challenge.

    @Test
    void ordersEndpoint_withWrongBasicCredentials_isNotRejected() throws Exception {
        String encoded = Base64.getEncoder().encodeToString("wrong:password".getBytes());
        mockMvc.perform(get("/orders").param("bar", "north")
                        .header("Authorization", "Basic " + encoded))
                .andExpect(status().isOk());
    }

    // ── No 401 / 403 on any endpoint ─────────────────────────────────────────

    @Test
    void getEndpoints_neverReturn401() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "north"))
                .andExpect(status().is(not(401)));
        mockMvc.perform(get("/products"))
                .andExpect(status().is(not(401)));
        mockMvc.perform(get("/bars"))
                .andExpect(status().is(not(401)));
        mockMvc.perform(get("/categories"))
                .andExpect(status().is(not(401)));
        mockMvc.perform(get("/events"))
                .andExpect(status().is(not(401)));
    }

    @Test
    void getEndpoints_neverReturn403() throws Exception {
        mockMvc.perform(get("/orders").param("bar", "north"))
                .andExpect(status().is(not(403)));
        mockMvc.perform(get("/products"))
                .andExpect(status().is(not(403)));
        mockMvc.perform(get("/bars"))
                .andExpect(status().is(not(403)));
        mockMvc.perform(get("/categories"))
                .andExpect(status().is(not(403)));
        mockMvc.perform(get("/events"))
                .andExpect(status().is(not(403)));
    }

    // ── CSRF disabled — POST works without a CSRF token ──────────────────────

    @Test
    void postAdvance_withoutCsrfToken_isNotForbidden() throws Exception {
        // Without CSRF disabled, Spring Security returns 403 for POST without a token.
        // With it disabled, the request is processed normally (may return 5xx if the
        // business logic fails, but must NOT return 403).
        mockMvc.perform(post("/orders/1/advance"))
                .andExpect(status().is(not(403)));
    }

    @Test
    void postCreate_withoutCsrfToken_isNotForbidden() throws Exception {
        String body = """
                {"items":[{"pid":"p1","q":1}],"bar":"north","total":8000}
                """;
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(not(403)));
    }
}
