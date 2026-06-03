package grupo4.fastbuyback.Repositories;

import grupo4.fastbuyback.Entities.Bar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarsRepository extends JpaRepository<Bar, String> {
    List<Bar> findByEventId(String eventId);
}
