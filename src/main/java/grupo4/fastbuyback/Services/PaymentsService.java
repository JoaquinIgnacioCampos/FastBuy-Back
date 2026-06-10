package grupo4.fastbuyback.Services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import grupo4.fastbuyback.Entities.Order;
import grupo4.fastbuyback.Entities.Product;
import grupo4.fastbuyback.Repositories.OrdersRepository;
import grupo4.fastbuyback.Repositories.ProductsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Builds a Mercado Pago Checkout Pro preference for an existing order and
 * returns the init_point URL the frontend redirects the user to.
 *
 * When mercadopago.access-token is blank (no MP credentials configured), this
 * service returns a "simulated" init_point that bounces the user straight back
 * to the frontend with status=approved — so the redirect plumbing is exercised
 * end-to-end without an MP account.
 */
@Service
public class PaymentsService {

    private static final Logger log = LoggerFactory.getLogger(PaymentsService.class);

    private final OrdersRepository ordersRepo;
    private final ProductsRepository productsRepo;
    private final PaymentAccountsService paymentAccountsService;
    private final String platformToken;
    private final boolean sandbox;
    private final String webOrigin;
    private final ObjectMapper mapper = new ObjectMapper();

    public PaymentsService(
            OrdersRepository ordersRepo,
            ProductsRepository productsRepo,
            PaymentAccountsService paymentAccountsService,
            @Value("${mercadopago.access-token:}") String platformToken,
            @Value("${mercadopago.sandbox:false}") boolean sandbox,
            @Value("${fastbuy.web-origin:http://localhost:5173}") String webOrigin
    ) {
        this.ordersRepo = ordersRepo;
        this.productsRepo = productsRepo;
        this.paymentAccountsService = paymentAccountsService;
        this.platformToken = platformToken;
        this.sandbox = sandbox;
        this.webOrigin = webOrigin;
    }

    public PreferenceResponse createPreference(String orderId) {
        Long id = parseId(orderId);
        Order order = ordersRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        String token = resolveToken(order);
        if (token == null) {
            return PreferenceResponse.simulated(simulatedReturnUrl(orderId, "approved"));
        }

        try {
            MercadoPagoConfig.setAccessToken(token);
            PreferenceRequest prefRequest = buildSDKRequest(order);
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

    private PreferenceRequest buildSDKRequest(Order order) {
        List<ItemDto> orderItems;
        try {
            orderItems = mapper.readValue(order.getItems(), new TypeReference<List<ItemDto>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse items for order " + order.getId(), e);
        }

        List<PreferenceItemRequest> mpItems = orderItems.stream().map(it -> {
            Product p = productsRepo.findById(it.pid()).orElse(null);
            String title = p != null ? p.getName() : it.pid();
            double price = p != null ? p.getPrice() : (order.getTotal() / Math.max(1, it.q()));
            return PreferenceItemRequest.builder()
                    .id(it.pid())
                    .title(title)
                    .quantity(it.q())
                    .unitPrice(BigDecimal.valueOf(price))
                    .currencyId("ARS")
                    .build();
        }).toList();

        String externalRef = "FB" + order.getId();
        return PreferenceRequest.builder()
                .items(mpItems)
                .backUrls(PreferenceBackUrlsRequest.builder()
                        .success(returnUrl(externalRef, "approved"))
                        .failure(returnUrl(externalRef, "rejected"))
                        .pending(returnUrl(externalRef, "pending"))
                        .build())
                .autoReturn("approved")
                .externalReference(externalRef)
                .build();
    }

    private String resolveToken(Order order) {
        try {
            String eventId = null;
            if (order.getBar() != null) {
                // TODO: inject BarsRepository to resolve eventId from bar for per-event tokens
            }
            if (eventId != null) {
                var perEvent = paymentAccountsService.getTokenForEvent(eventId);
                if (perEvent.isPresent()) return perEvent.get();
            }
        } catch (Exception ignored) {}

        if (platformToken != null && !platformToken.isBlank()) return platformToken;
        return null;
    }

    private String returnUrl(String externalRef, String status) {
        return webOrigin
                + "/?status=" + URLEncoder.encode(status, StandardCharsets.UTF_8)
                + "&external_reference=" + URLEncoder.encode(externalRef, StandardCharsets.UTF_8);
    }

    private String simulatedReturnUrl(String orderId, String status) {
        return returnUrl(orderId, status);
    }

    private static Long parseId(String orderId) {
        String clean = orderId.startsWith("FB") ? orderId.substring(2) : orderId;
        try {
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid order id: " + orderId);
        }
    }
}
