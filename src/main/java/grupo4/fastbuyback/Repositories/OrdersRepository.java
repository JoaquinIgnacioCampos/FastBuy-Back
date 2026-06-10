package grupo4.fastbuyback.Repositories;

import grupo4.fastbuyback.Entities.Order;
import grupo4.fastbuyback.Entities.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrdersRepository extends JpaRepository<Order, Long> {

    List<Order> findByBar(String bar);

    List<Order> findByBarAndStatusIn(String bar, List<OrderState> statuses);

    List<Order> findTop50ByBarAndStatusOrderByIdDesc(String bar, OrderState status);

    List<Order> findByBarAndStatusAndClaimedAtBefore(String bar, OrderState status, LocalDateTime threshold);
}
