package grupo4.fastbuyback.Controllers;

import grupo4.fastbuyback.Entities.Bar;
import grupo4.fastbuyback.Entities.Event;
import grupo4.fastbuyback.Entities.Product;
import grupo4.fastbuyback.Services.BarsService;
import grupo4.fastbuyback.Services.EventsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventsController {

    private final EventsService eventsService;
    private final BarsService barsService;

    public EventsController(EventsService eventsService, BarsService barsService) {
        this.eventsService = eventsService;
        this.barsService   = barsService;
    }

    @GetMapping
    public List<Event> getActive() {
        return eventsService.getActive();
    }

    @GetMapping("/{eventId}/bars")
    public List<Bar> getBarsForEvent(@PathVariable String eventId) {
        return barsService.getByEvent(eventId);
    }

    @GetMapping("/{eventId}/menu")
    public List<Product> getMenuForEvent(@PathVariable String eventId) {
        return barsService.getMenuForEvent(eventId);
    }
}
