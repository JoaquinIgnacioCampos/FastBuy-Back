package grupo4.fastbuyback.Services;

import grupo4.fastbuyback.Entities.Event;
import grupo4.fastbuyback.Repositories.EventsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventsService {

    private static final long GRACE_PERIOD_HOURS = 6;

    private final EventsRepository repo;

    public EventsService(EventsRepository repo) {
        this.repo = repo;
    }

    public List<Event> getActive() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(GRACE_PERIOD_HOURS);
        return repo.findActiveSince(cutoff);
    }
}
