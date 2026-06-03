package grupo4.fastbuyback.Repositories;

import grupo4.fastbuyback.Entities.PaymentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentAccountsRepository extends JpaRepository<PaymentAccount, String> {
    Optional<PaymentAccount> findByEventId(String eventId);
    void deleteByEventId(String eventId);
}
