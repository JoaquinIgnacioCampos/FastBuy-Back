package grupo4.fastbuyback.Repositories;

import grupo4.fastbuyback.Entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventsRepository extends JpaRepository<Event, String> {

    @Query("SELECT e FROM Event e WHERE e.endsAt >= :cutoff ORDER BY e.startsAt ASC")
    List<Event> findActiveSince(@Param("cutoff") LocalDateTime cutoff);
}
