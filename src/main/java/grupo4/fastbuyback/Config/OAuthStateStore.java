package grupo4.fastbuyback.Config;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-lived in-memory store for OAuth state nonces.
 * Maps a random nonce → eventId; entries expire after 10 minutes.
 * Single-instance safe; no persistence needed (OAuth roundtrip is fast).
 */
@Component
public class OAuthStateStore {

    private static final long TTL_MS = 10 * 60 * 1_000;

    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    /** Creates and stores a nonce for the given eventId. Returns the nonce. */
    public String create(String eventId) {
        cleanup();
        String nonce = UUID.randomUUID().toString();
        store.put(nonce, new Entry(eventId, System.currentTimeMillis()));
        return nonce;
    }

    /**
     * Validates and removes the nonce. Returns the associated eventId if the
     * nonce is found and not expired, or empty otherwise.
     */
    public Optional<String> consume(String nonce) {
        Entry e = store.remove(nonce);
        if (e == null) return Optional.empty();
        if (System.currentTimeMillis() - e.createdAt() > TTL_MS) return Optional.empty();
        return Optional.of(e.eventId());
    }

    private void cleanup() {
        long threshold = System.currentTimeMillis() - TTL_MS;
        store.values().removeIf(e -> e.createdAt() < threshold);
    }

    private record Entry(String eventId, long createdAt) {}
}
