package grupo4.fastbuyback.Controllers;

import grupo4.fastbuyback.DTOs.LinkAccountRequest;
import grupo4.fastbuyback.DTOs.PaymentAccountResponse;
import grupo4.fastbuyback.Services.PaymentAccountsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events/{eventId}/payment-account")
public class PaymentAccountsController {

    private final PaymentAccountsService service;

    public PaymentAccountsController(PaymentAccountsService service) {
        this.service = service;
    }

    @GetMapping
    public PaymentAccountResponse get(@PathVariable String eventId) {
        return service.getForEvent(eventId);
    }

    @PostMapping
    public ResponseEntity<PaymentAccountResponse> link(
            @PathVariable String eventId,
            @Valid @RequestBody LinkAccountRequest req) {
        try {
            return ResponseEntity.ok(service.linkEvent(eventId, req.accessToken()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> unlink(@PathVariable String eventId) {
        service.unlinkEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}
