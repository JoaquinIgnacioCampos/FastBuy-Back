package grupo4.fastbuyback.Repositories;

import grupo4.fastbuyback.Entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventsRepository extends JpaRepository<Event, String> {}
