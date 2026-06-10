package grupo4.fastbuyback.Services;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import grupo4.fastbuyback.DTOs.ItemDto;
import grupo4.fastbuyback.DTOs.PreferenceResponse;
import grupo4.fastbuyback.Entities.Product;
import grupo4.fastbuyback.Repositories.ProductsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Builds a Mercado Pago Checkout Pro preference from the cart items and returns
 * the checkout URL. No order is created here — the order is created only after
 * the customer returns with status=approved.
 *
 * When mercadopago.access-token is blank this service returns a "simulated"
 * init_point that bounces straight back to the frontend with status=approved.
 */
@Service
public class PaymentsService {

    private static final Logger log = LoggerFactory.getLogger(PaymentsService.class);

    private final ProductsRepository productsRepo;
    private final PaymentAccountsService paymentAccountsService;
    private final String platformToken;
    private final boolean sandbox;
    private final String webOrigin;

    public PaymentsService(
            ProductsRepository productsRepo,
            PaymentAccountsService paymentAccountsService,
            @Value("${mercadopago.access-token:}") String platformToken,
            @Value("${mercadopago.sandbox:false}") boolean sandbox,
            @Value("${fastbuy.web-origin:http://localhost:5173}") String webOrigin
    ) {
        this.productsRepo = productsRepo;
        this.paymentAccountsService = paymentAccountsService;
        this.platformToken = platformToken;
        this.sandbox = sandbox;
        this.webOrigin = webOrigin;
    }

    public PreferenceResponse createPreference(List<ItemDto> items, double total, String eventId) {
        String token = resolveToken(eventId);
        String sessionRef = UUID.randomUUID().toString();

        if (token == null) {
            return PreferenceResponse.simulated(returnUrl(sessionRef, "approved"));
        }

        try {
            MercadoPagoConfig.setAccessToken(token);
            PreferenceRequest prefRequest = buildSDKRequest(items, total, sessionRef);
            Preference pref = new PreferenceClient().create(prefRequest);

            String checkoutUrl = sandbox ? pref.getSandboxInitPoint() : pref.getInitPoint();
            log.info("MP preference created: id={} sandbox={} url={}", pref.getId(), sandbox, checkoutUrl);

            return PreferenceResponse.real(pref.getId(), checkoutUrl, pref.getSandboxInitPoint());

        } catch (MPApiException e) {
            log.error("MP API error: status={} body={}", e.getStatusCode(), e.getApiResponse().getContent());
            throw new RuntimeException("Mercado Pago API error: " + e.getMessage(), e);
        } catch (MPException e) {
            throw new RuntimeException("Mercado Pago connection error", e);
        }
    }

    private PreferenceRequest buildSDKRequest(List<ItemDto> items, double total, String sessionRef) {
        List<PreferenceItemRequest> mpItems = items.stream().map(it -> {
            Product p = productsRepo.findById(it.pid()).orElse(null);
            String title = p != null ? p.getName() : it.pid();
            double price = p != null ? p.getPrice() : (total / Math.max(1, it.q()));
            return PreferenceItemRequest.builder()
                    .id(it.pid())
                    .title(title)
                    .quantity(it.q())
                    .unitPrice(BigDecimal.valueOf(price))
                    .currencyId("ARS")
                    .build();
        }).toList();

        PreferenceRequest.PreferenceRequestBuilder builder = PreferenceRequest.builder()
                .items(mpItems)
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(returnUrl(sessionRef, "approved"))
                        .failure(returnUrl(sessionRef, "rejected"))
                        .pending(returnUrl(sessionRef, "pending"))
                        .build())
                .externalReference(sessionRef);

        // auto_return causes too-many-redirects in sandbox due to extra MP hops
        if (!sandbox) builder.autoReturn("approved");

        return builder.build();
    }

    private String resolveToken(String eventId) {
        if (eventId != null && !eventId.isBlank()) {
            try {
                var perEvent = paymentAccountsService.getTokenForEvent(eventId);
                if (perEvent.isPresent()) return perEvent.get();
            } catch (Exception ignored) {}
        }
        if (platformToken != null && !platformToken.isBlank()) return platformToken;
        return null;
    }

    private String returnUrl(String ref, String status) {
        return webOrigin
                + "/?status=" + URLEncoder.encode(status, StandardCharsets.UTF_8)
                + "&external_reference=" + URLEncoder.encode(ref, StandardCharsets.UTF_8);
    }
}
