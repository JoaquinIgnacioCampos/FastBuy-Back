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

    /** Aggregate over the orders ahead of a given order at the same bar (one query). */
    interface QueueAhead {
        long getOrdersAhead();  // orders still in front, incl. ready-not-delivered (for position)
        long getItemsAhead();   // beverages still to be MADE ahead, ready excluded (for ETA)
    }

    // countStatuses widens the position (QUEUE+PREPARING+READY); prepStatuses
    // narrows the ETA sum to drinks not yet made (QUEUE+PREPARING) — a READY order
    // is already prepared, so it occupies a line slot but adds no prep time.
    @Query("SELECT COUNT(o) AS ordersAhead, "
         + "COALESCE(SUM(CASE WHEN o.status IN :prepStatuses THEN o.itemCount ELSE 0 END), 0) AS itemsAhead "
         + "FROM Order o WHERE o.bar = :bar AND o.status IN :countStatuses AND o.id < :id")
    QueueAhead queueAhead(@Param("bar") String bar,
                          @Param("countStatuses") List<OrderState> countStatuses,
                          @Param("prepStatuses") List<OrderState> prepStatuses,
                          @Param("id") Long id);

    /** Frees claimed PREPARING orders older than the threshold back to QUEUE, in one statement. */
    @Modifying
    @Query("UPDATE Order o SET o.status = grupo4.fastbuyback.Entities.OrderState.QUEUE, "
         + "o.claimedBy = null, o.claimedAt = null "
         + "WHERE o.status = grupo4.fastbuyback.Entities.OrderState.PREPARING AND o.claimedAt < :threshold")
    int expireStaleClaims(@Param("threshold") LocalDateTime threshold);
}
