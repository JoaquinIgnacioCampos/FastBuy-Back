package grupo4.fastbuyback.Controllers;

import grupo4.fastbuyback.DTOs.CreateOrderRequest;
import grupo4.fastbuyback.DTOs.OrderResponse;
import grupo4.fastbuyback.Services.OrdersService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final OrdersService service;

    public OrdersController(OrdersService service) {
        this.service = service;
    }

    // Phase 1 — Bartender flow

    @GetMapping
    public List<OrderResponse> getOrders(@RequestParam String bar) {
        return service.getOrdersByBar(bar);
    }

    @PostMapping("/{id}/advance")
    public OrderResponse advance(@PathVariable String id) {
        return service.advanceOrder(parseId(id));
    }

    @PostMapping("/{id}/deliver")
    public OrderResponse deliver(@PathVariable String id) {
        return service.deliverOrder(parseId(id));
    }

    // Phase 2 — Order creation

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest req) {
        return ResponseEntity.status(201).body(service.createOrder(req));
    }

    // Strip optional "FB" prefix so both "1" and "FB1" work as path variables
    private Long parseId(String id) {
        String clean = id.startsWith("FB") ? id.substring(2) : id;
        return Long.parseLong(clean);
    }
}
