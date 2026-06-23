package grupo4.fastbuyback.Repositories;

import grupo4.fastbuyback.Entities.Order;
import grupo4.fastbuyback.Entities.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrdersRepository extends JpaRepository<Order, Long> {

    List<Order> findByBarAndStatusInOrderByIdAsc(String bar, List<OrderState> statuses);

    List<Order> findTop50ByBarAndStatusOrderByIdDesc(String bar, OrderState status);

    int countByBarAndStatusAndIdLessThan(String bar, OrderState status, Long id);

    /** Frees claimed PREPARING orders older than the threshold back to QUEUE, in one statement. */
    @Modifying
    @Query("UPDATE Order o SET o.status = grupo4.fastbuyback.Entities.OrderState.QUEUE, "
         + "o.claimedBy = null, o.claimedAt = null "
         + "WHERE o.status = grupo4.fastbuyback.Entities.OrderState.PREPARING AND o.claimedAt < :threshold")
    int expireStaleClaims(@Param("threshold") LocalDateTime threshold);
}
