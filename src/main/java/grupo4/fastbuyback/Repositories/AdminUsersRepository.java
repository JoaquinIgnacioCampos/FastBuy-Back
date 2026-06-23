package grupo4.fastbuyback.Repositories;

import grupo4.fastbuyback.Entities.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUsersRepository extends JpaRepository<AdminUser, String> {
    Optional<AdminUser> findByUsername(String username);
    Optional<AdminUser> findBySessionToken(String sessionToken);
}
