package grupo4.fastbuyback.Services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import grupo4.fastbuyback.DTOs.CreateOrderRequest;
import grupo4.fastbuyback.DTOs.ItemDto;
import grupo4.fastbuyback.DTOs.OrderResponse;
import grupo4.fastbuyback.Entities.Order;
import grupo4.fastbuyback.Entities.OrderState;
import grupo4.fastbuyback.Repositories.OrdersRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrdersService {

    private final OrdersRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public OrdersService(OrdersRepository repo) {
        this.repo = repo;
    }

    public List<OrderResponse> getOrdersByBar(String barId) {
        List<OrderState> active = List.of(OrderState.QUEUE, OrderState.PREPARING, OrderState.READY);
        return repo.findByBarAndStatusIn(barId, active)
                   .stream()
                   .map(this::toResponse)
                   .toList();
    }

    public OrderResponse advanceOrder(Long id) {
        Order order = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        OrderState next = switch (order.getStatus()) {
            case QUEUE     -> OrderState.PREPARING;
            case PREPARING -> OrderState.READY;
            default        -> throw new IllegalStateException(
                "Cannot advance order from status: " + order.getStatus()
            );
        };

        order.setStatus(next);
        return toResponse(repo.save(order));
    }

    public OrderResponse deliverOrder(Long id) {
        Order order = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        if (order.getStatus() != OrderState.READY) {
            throw new IllegalStateException("Order must be READY to deliver, current status: " + order.getStatus());
        }

        order.setStatus(OrderState.DELIVERED);
        return toResponse(repo.save(order));
    }

    public OrderResponse createOrder(CreateOrderRequest req) {
        try {
            Order order = new Order();
            order.setTotal(req.total());
            order.setBar(req.bar());
            order.setStatus(OrderState.QUEUE);
            order.setItems(mapper.writeValueAsString(req.items()));
            order.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm")));
            return toResponse(repo.save(order));
        } catch (Exception e) {
            throw new RuntimeException("Failed to create order", e);
        }
    }

    private OrderResponse toResponse(Order order) {
        try {
            List<ItemDto> items = mapper.readValue(
                order.getItems(),
                new TypeReference<List<ItemDto>>() {}
            );
            return new OrderResponse(
                "FB" + order.getId(),
                order.getTotal(),
                items,
                order.getStatus(),
                order.getBar(),
                order.getTime()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse items for order " + order.getId(), e);
        }
    }
}
