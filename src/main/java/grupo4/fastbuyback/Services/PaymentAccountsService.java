package grupo4.fastbuyback.Services;

import grupo4.fastbuyback.DTOs.PaymentAccountResponse;
import grupo4.fastbuyback.Entities.Event;
import grupo4.fastbuyback.Entities.PaymentAccount;
import grupo4.fastbuyback.Repositories.EventsRepository;
import grupo4.fastbuyback.Repositories.PaymentAccountsRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentAccountsService {

    private static final String MP_USERS_ME = "https://api.mercadopago.com/users/me";

    private final PaymentAccountsRepository repo;
    private final EventsRepository eventsRepo;
    private final TextEncryptor encryptor;
    private final RestClient http;

    public PaymentAccountsService(PaymentAccountsRepository repo,
                                  EventsRepository eventsRepo,
                                  TextEncryptor encryptor) {
        this.repo      = repo;
        this.eventsRepo = eventsRepo;
        this.encryptor = encryptor;
        this.http      = RestClient.create();
    }

    public PaymentAccountResponse getForEvent(String eventId) {
        Optional<PaymentAccount> opt = repo.findByEventId(eventId);
        if (opt.isEmpty()) return PaymentAccountResponse.unlinked();

        PaymentAccount pa = opt.get();
        String email = fetchMpEmail(encryptor.decrypt(pa.getAccessToken()));
        return new PaymentAccountResponse(true, pa.getMpUserId(), email, pa.getLinkedAt());
    }

    @Transactional
    public PaymentAccountResponse linkEvent(String eventId, String accessToken) {
        Event event = eventsRepo.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        Map<String, Object> mpUser = fetchMpUser(accessToken);
        String mpUserId = String.valueOf(mpUser.get("id"));
        String mpEmail  = String.valueOf(mpUser.getOrDefault("email", ""));

        PaymentAccount pa = repo.findByEventId(eventId).orElseGet(() -> {
            PaymentAccount n = new PaymentAccount();
            n.setId(UUID.randomUUID().toString());
            n.setEvent(event);
            return n;
        });

        pa.setAccessToken(encryptor.encrypt(accessToken));
        pa.setMpUserId(mpUserId);
        pa.setLinkedAt(LocalDateTime.now());
        repo.save(pa);

        return new PaymentAccountResponse(true, mpUserId, mpEmail, pa.getLinkedAt());
    }

    @Transactional
    public void unlinkEvent(String eventId) {
        repo.deleteByEventId(eventId);
    }

    /** Returns the access token for an event, or null if not linked. */
    public Optional<String> getTokenForEvent(String eventId) {
        return repo.findByEventId(eventId).map(pa -> encryptor.decrypt(pa.getAccessToken()));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchMpUser(String token) {
        try {
            return http.get()
                .uri(MP_USERS_ME)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(Map.class);
        } catch (RestClientException e) {
            throw new IllegalArgumentException("Invalid Mercado Pago access token: " + e.getMessage());
        }
    }

    private String fetchMpEmail(String token) {
        try {
            Map<String, Object> user = fetchMpUser(token);
            return String.valueOf(user.getOrDefault("email", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
