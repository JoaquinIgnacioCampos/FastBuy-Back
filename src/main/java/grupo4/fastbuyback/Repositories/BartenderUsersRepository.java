package grupo4.fastbuyback.Repositories;

import grupo4.fastbuyback.Entities.BartenderUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BartenderUsersRepository extends JpaRepository<BartenderUser, String> {
    Optional<BartenderUser> findByUsername(String username);
}
