package grupo4.fastbuyback.Controllers;

import grupo4.fastbuyback.Entities.Bar;
import grupo4.fastbuyback.Services.BarsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bars")
public class BarsController {

    private final BarsService service;

    public BarsController(BarsService service) {
        this.service = service;
    }

    @GetMapping
    public List<Bar> getAll() {
        return service.getAll();
    }
}
