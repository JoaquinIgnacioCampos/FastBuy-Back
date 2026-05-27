package grupo4.fastbuyback.Services;

import grupo4.fastbuyback.Entities.Event;
import grupo4.fastbuyback.Repositories.EventsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventsService {

    private final EventsRepository repo;

    public EventsService(EventsRepository repo) {
        this.repo = repo;
    }

    public List<Event> getAll() {
        return repo.findAll();
    }
}
