package grupo4.fastbuyback.Services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import grupo4.fastbuyback.DTOs.CreateOrderRequest;
import grupo4.fastbuyback.DTOs.ItemDto;
import grupo4.fastbuyback.DTOs.OrderResponse;
import grupo4.fastbuyback.Entities.Bar;
import grupo4.fastbuyback.Entities.Order;
import grupo4.fastbuyback.Entities.OrderState;
import grupo4.fastbuyback.Entities.Product;
import grupo4.fastbuyback.Repositories.BarsRepository;
import grupo4.fastbuyback.Repositories.OrdersRepository;
import grupo4.fastbuyback.Repositories.ProductsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrdersService {

    private static final List<OrderState> ACTIVE_FOR_LOAD =
            List.of(OrderState.QUEUE, OrderState.PREPARING);
    private static final List<OrderState> ACTIVE_FOR_BAR_VIEW =
            List.of(OrderState.QUEUE, OrderState.PREPARING, OrderState.READY);

    private final OrdersRepository repo;
    private final BarsRepository barsRepo;
    private final ProductsRepository productsRepo;
    private static final ObjectMapper mapper = new ObjectMapper();

    public OrdersService(OrdersRepository repo, BarsRepository barsRepo, ProductsRepository productsRepo) {
        this.repo = repo;
        this.barsRepo = barsRepo;
        this.productsRepo = productsRepo;
    }

    // A claimed PREPARING order is only auto-released back to QUEUE after this long
    // with no progress — long enough that an actively-working bartender never trips
    // it (a 2-minute window reverted orders mid-preparation, breaking "mark ready").
    private static final long CLAIM_EXPIRY_MINUTES = 15;

    public List<OrderResponse> getOrdersByBar(String barId) {
        // Lazy expiry: a PREPARING order whose lock is older than the window returns to QUEUE
        LocalDateTime expiry = LocalDateTime.now().minusMinutes(CLAIM_EXPIRY_MINUTES);
        List<Order> expired = repo.findByBarAndStatusAndClaimedAtBefore(barId, OrderState.PREPARING, expiry);
        for (Order o : expired) {
            o.setStatus(OrderState.QUEUE);
            o.setClaimedBy(null);
            o.setClaimedAt(null);
            repo.save(o);
        }

        // All orders in this list share one bar, so resolve its label once.
        String barLabel = barLabelFor(barId);
        return repo.findByBarAndStatusInOrderByIdAsc(barId, ACTIVE_FOR_BAR_VIEW)
                   .stream()
                   .map(o -> toResponse(o, barLabel))
                   .toList();
    }

    public List<OrderResponse> getDeliveredByBar(String barId) {
        String barLabel = barLabelFor(barId);
        return repo.findTop50ByBarAndStatusOrderByIdDesc(barId, OrderState.DELIVERED)
                   .stream()
                   .map(o -> toResponse(o, barLabel))
                   .toList();
    }

    public OrderResponse advanceOrder(Long id, String bartenderId) {
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
        if (next == OrderState.PREPARING && bartenderId != null && !bartenderId.isBlank()) {
            order.setClaimedBy(bartenderId);
            order.setClaimedAt(LocalDateTime.now());
        }
        return toResponse(repo.save(order));
    }

    public OrderResponse releaseOrder(Long id) {
        Order order = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        if (order.getStatus() != OrderState.PREPARING) {
            throw new IllegalStateException("Only PREPARING orders can be released, current status: " + order.getStatus());
        }

        order.setStatus(OrderState.QUEUE);
        order.setClaimedBy(null);
        order.setClaimedAt(null);
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

    public OrderResponse cancelOrder(Long id) {
        Order order = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));

        if (order.getStatus() != OrderState.READY) {
            throw new IllegalStateException("Only READY orders can be cancelled, current status: " + order.getStatus());
        }

        // No-show: the customer never came to pick up. Drop the order off the
        // active board (and free the bartender) via a terminal CANCELLED state.
        order.setStatus(OrderState.CANCELLED);
        order.setClaimedBy(null);
        order.setClaimedAt(null);
        return toResponse(repo.save(order));
    }

    /** Single order by id, in any state (queue/preparing/ready/delivered/cancelled). */
    public OrderResponse getOrder(Long id) {
        Order order = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        return toResponse(order);
    }

    public OrderResponse createOrder(CreateOrderRequest req) {
        try {
            String barId = (req.bar() == null || req.bar().isBlank())
                    ? pickLeastLoadedBar(req.items(), req.eventId())
                    : req.bar();

            Order order = new Order();
            order.setTotal(req.total());
            order.setBar(barId);
            order.setStatus(OrderState.QUEUE);
            order.setItems(mapper.writeValueAsString(req.items()));
            order.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("H:mm")));
            return toResponse(repo.save(order));
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create order", e);
        }
    }

    /**
     * Picks a bar for the requested items. Two-phase:
     *   1. Strict — bars whose menu contains every requested PID; pick least-loaded.
     *   2. Lenient — if no bar serves all items, pick the bar that serves the
     *      most of them, breaking ties by least load. Always returns a bar
     *      (or throws if the DB has no bars at all).
     * Load = sum of item quantities across QUEUE+PREPARING orders for that bar.
     */
    private String pickLeastLoadedBar(List<ItemDto> items, String eventId) {
        Set<String> wanted = items.stream().map(ItemDto::pid).collect(Collectors.toSet());

        List<Bar> bars = (eventId != null && !eventId.isBlank())
                ? barsRepo.findByEventId(eventId)
                : barsRepo.findAll();
        if (bars.isEmpty()) {
            throw new IllegalStateException("No bars are configured");
        }

        String strictBest = null;
        int strictBestLoad = Integer.MAX_VALUE;

        String lenientBest = null;
        int lenientBestMatches = -1;
        int lenientBestLoad = Integer.MAX_VALUE;

        for (Bar bar : bars) {
            Set<String> menu = productsRepo.findByBarId(bar.getId())
                                           .stream()
                                           .map(Product::getId)
                                           .collect(Collectors.toSet());
            int matches = (int) wanted.stream().filter(menu::contains).count();
            int load = repo.findByBarAndStatusInOrderByIdAsc(bar.getId(), ACTIVE_FOR_LOAD)
                           .stream()
                           .mapToInt(this::sumItemQuantities)
                           .sum();

            if (matches == wanted.size() && load < strictBestLoad) {
                strictBestLoad = load;
                strictBest = bar.getId();
            }
            if (matches > lenientBestMatches
                    || (matches == lenientBestMatches && load < lenientBestLoad)) {
                lenientBestMatches = matches;
                lenientBestLoad = load;
                lenientBest = bar.getId();
            }
        }

        return strictBest != null ? strictBest : lenientBest;
    }

    private int sumItemQuantities(Order o) {
        try {
            List<ItemDto> parsed = mapper.readValue(
                    o.getItems(),
                    new TypeReference<List<ItemDto>>() {}
            );
            return parsed.stream().mapToInt(ItemDto::q).sum();
        } catch (Exception e) {
            return 0;
        }
    }

    private OrderResponse toResponse(Order order) {
        return toResponse(order, barLabelFor(order.getBar()));
    }

    private OrderResponse toResponse(Order order, String barLabel) {
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
                barLabel,
                order.getTime(),
                order.getClaimedBy()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse items for order " + order.getId(), e);
        }
    }

    /** Friendly bar name (e.g. "Barra Norte") for a bar id; falls back to the id. */
    private String barLabelFor(String barId) {
        return barsRepo.findById(barId).map(Bar::getLabel).orElse(barId);
    }
}
