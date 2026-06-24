package grupo4.fastbuyback.Controllers;

import grupo4.fastbuyback.Config.OAuthStateStore;
import grupo4.fastbuyback.Repositories.AdminUsersRepository;
import grupo4.fastbuyback.Services.PaymentAccountsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Mercado Pago OAuth seller-onboarding flow.
 *
 * GET /auth/mp/connect  — validates the admin session, returns {"url":"..."} JSON.
 *                         The frontend navigates window.location.href to that URL
 *                         so the Vite dev-proxy never sees the redirect.
 * GET /auth/mp/callback — MP redirects here with ?code + ?state; the code is
 *                         exchanged for an access token and stored via the
 *                         existing PaymentAccountsService.linkEvent().
 *
 * Required env vars: MP_APP_ID, MP_APP_SECRET, MP_REDIRECT_URI.
 * MP_REDIRECT_URI must be registered in the MP app's allowed redirect URIs.
 * Example local value: https://crepe-phonics-slogan.ngrok-free.dev/api/auth/mp/callback
 * Example prod value:  https://fastbuy-back.onrender.com/auth/mp/callback
 */
@RestController
@RequestMapping("/auth/mp")
public class MpOAuthController {

    private static final Logger log = LoggerFactory.getLogger(MpOAuthController.class);

    private final OAuthStateStore stateStore;
    private final AdminUsersRepository adminRepo;
    private final PaymentAccountsService paymentAccountsService;
    private final RestClient http;

    @Value("${mp.app.id:}")          private String appId;
    @Value("${mp.app.secret:}")      private String appSecret;
    @Value("${mp.redirect.uri:}")    private String redirectUri;
    @Value("${fastbuy.web-origin:http://localhost:5173}") private String frontendUrl;

    public MpOAuthController(OAuthStateStore stateStore,
                             AdminUsersRepository adminRepo,
                             PaymentAccountsService paymentAccountsService) {
        this.stateStore             = stateStore;
        this.adminRepo              = adminRepo;
        this.paymentAccountsService = paymentAccountsService;
        this.http                   = RestClient.create();
    }

    /**
     * Returns the MP authorization URL as JSON so the frontend can navigate
     * directly. Returning a 302 here would cause the Vite dev-proxy to attempt
     * to proxy the external MP URL, which fails.
     */
    @GetMapping("/connect")
    public ResponseEntity<Map<String, String>> connect(
            @RequestParam String eventId,
            @RequestParam String token) {

        if (appId.isBlank() || redirectUri.isBlank()) {
            log.warn("MP OAuth not configured: MP_APP_ID or MP_REDIRECT_URI is blank");
            return ResponseEntity.ok(Map.of("error", "not_configured"));
        }

        adminRepo.findBySessionToken(token)
                .filter(a -> a.getEvent().getId().equals(eventId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid admin session for this event"));

        String state   = stateStore.create(eventId);
        String authUrl = "https://auth.mercadopago.com/authorization"
                + "?client_id="     + appId
                + "&response_type=code"
                + "&platform_id=mp"
                + "&state="         + state
                + "&redirect_uri="  + encode(redirectUri);

        log.info("MP OAuth: redirecting eventId={} to MP auth", eventId);
        return ResponseEntity.ok(Map.of("url", authUrl));
    }

    /**
     * MP redirects here after the seller approves (or cancels) access.
     * Any failure redirects to the frontend with an error code instead of
     * throwing a 500 whitelabel page.
     */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false, defaultValue = "") String error) {

        if (!error.isBlank() || code == null || state == null) {
            log.info("MP OAuth callback: access denied or missing params (error={})", error);
            return redirect(frontendUrl + "/admin?mp_error=access_denied");
        }

        try {
            String eventId = stateStore.consume(state)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OAuth state: " + state));

            log.info("MP OAuth callback: exchanging code for eventId={}", eventId);
            String accessToken = exchangeCode(code);
            paymentAccountsService.linkEvent(eventId, accessToken);
            log.info("MP OAuth callback: linked account for eventId={}", eventId);

            return redirect(frontendUrl + "/admin?mp_linked=true");

        } catch (Exception e) {
            log.error("MP OAuth callback failed: {}", e.getMessage(), e);
            return redirect(frontendUrl + "/admin?mp_error=callback_failed");
        }
    }

    // ── private helpers ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String exchangeCode(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id",     appId);
        params.add("client_secret", appSecret);
        params.add("grant_type",    "authorization_code");
        params.add("code",          code);
        params.add("redirect_uri",  redirectUri);

        Map<String, Object> body = http.post()
                .uri("https://api.mercadopago.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(Map.class);

        Object tkn = body != null ? body.get("access_token") : null;
        if (tkn == null) throw new IllegalStateException("MP returned no access_token in exchange response");
        return String.valueOf(tkn);
    }

    private static ResponseEntity<Void> redirect(String url) {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
