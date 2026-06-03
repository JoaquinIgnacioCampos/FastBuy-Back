package grupo4.fastbuyback.Controllers;

import grupo4.fastbuyback.DTOs.PreferenceRequest;
import grupo4.fastbuyback.DTOs.PreferenceResponse;
import grupo4.fastbuyback.Services.PaymentsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentsController {

    private final PaymentsService service;

    public PaymentsController(PaymentsService service) {
        this.service = service;
    }

    @PostMapping("/preference")
    public PreferenceResponse createPreference(@Valid @RequestBody PreferenceRequest req) {
        return service.createPreference(req.orderId());
    }
}
