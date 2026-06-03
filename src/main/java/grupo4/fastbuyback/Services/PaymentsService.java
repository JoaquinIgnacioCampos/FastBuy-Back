package grupo4.fastbuyback.Services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import grupo4.fastbuyback.DTOs.ItemDto;
import grupo4.fastbuyback.DTOs.PreferenceResponse;
import grupo4.fastbuyback.Entities.Order;
import grupo4.fastbuyback.Entities.Product;
import grupo4.fastbuyback.Repositories.OrdersRepository;
import grupo4.fastbuyback.Repositories.ProductsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static final String MP_PREFERENCES_URL = "https://api.mercadopago.com/checkout/preferences";

    private final OrdersRepository ordersRepo;
    private final ProductsRepository productsRepo;
    private final String accessToken;
    private final String webOrigin;
    private final RestClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public PaymentsService(
            OrdersRepository ordersRepo,
            ProductsRepository productsRepo,
            @Value("${mercadopago.access-token:}") String accessToken,
            @Value("${fastbuy.web-origin:http://localhost:5173}") String webOrigin
    ) {
        this.ordersRepo = ordersRepo;
        this.productsRepo = productsRepo;
        this.accessToken = accessToken;
        this.webOrigin = webOrigin;
        this.http = RestClient.create();
    }

    public PreferenceResponse createPreference(String orderId) {
        Long id = parseId(orderId);
        Order order = ordersRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        if (accessToken == null || accessToken.isBlank()) {
            return PreferenceResponse.simulated(simulatedReturnUrl(orderId, "approved"));
        }

        Map<String, Object> body = buildPreferenceBody(order);
        @SuppressWarnings("unchecked")
        Map<String, Object> resp = http.post()
                .uri(MP_PREFERENCES_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (resp == null) {
            throw new IllegalStateException("Empty response from Mercado Pago");
        }
        return PreferenceResponse.real(
                stringOrNull(resp.get("id")),
                stringOrNull(resp.get("init_point")),
                stringOrNull(resp.get("sandbox_init_point"))
        );
    }

    private Map<String, Object> buildPreferenceBody(Order order) {
        List<ItemDto> orderItems;
        try {
            orderItems = mapper.readValue(order.getItems(), new TypeReference<List<ItemDto>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse items for order " + order.getId(), e);
        }

        List<Map<String, Object>> mpItems = new ArrayList<>();
        for (ItemDto it : orderItems) {
            Product p = productsRepo.findById(it.pid()).orElse(null);
            Map<String, Object> mpItem = new HashMap<>();
            mpItem.put("id", it.pid());
            mpItem.put("title", p != null ? p.getName() : it.pid());
            mpItem.put("quantity", it.q());
            mpItem.put("currency_id", "ARS");
            mpItem.put("unit_price", p != null ? p.getPrice() : (order.getTotal() / Math.max(1, it.q())));
            mpItems.add(mpItem);
        }

        String externalRef = "FB" + order.getId();
        Map<String, Object> backUrls = Map.of(
                "success", returnUrl(externalRef, "approved"),
                "pending", returnUrl(externalRef, "pending"),
                "failure", returnUrl(externalRef, "rejected")
        );

        Map<String, Object> body = new HashMap<>();
        body.put("items", mpItems);
        body.put("external_reference", externalRef);
        body.put("back_urls", backUrls);
        body.put("auto_return", "approved");
        return body;
    }

    private String returnUrl(String externalRef, String status) {
        return webOrigin
                + "/?status=" + URLEncoder.encode(status, StandardCharsets.UTF_8)
                + "&external_reference=" + URLEncoder.encode(externalRef, StandardCharsets.UTF_8);
    }

    private String simulatedReturnUrl(String orderId, String status) {
        return returnUrl(orderId, status);
    }

    private static String stringOrNull(Object v) {
        return v == null ? null : v.toString();
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
