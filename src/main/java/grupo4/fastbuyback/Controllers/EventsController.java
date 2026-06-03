package grupo4.fastbuyback.Controllers;

import grupo4.fastbuyback.Entities.Event;
import grupo4.fastbuyback.Services.EventsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventsController {

    private final EventsService service;

    public EventsController(EventsService service) {
        this.service = service;
    }

    @GetMapping
    public List<Event> getActive() {
        return service.getActive();
    }
}
