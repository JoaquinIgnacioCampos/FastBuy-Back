package grupo4.fastbuyback.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_accounts")
@Getter
@Setter
@NoArgsConstructor
public class PaymentAccount {

    @Id
    private String id;

    @Column(name = "event_id", insertable = false, updatable = false)
    private String eventId;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, unique = true)
    private Event event;

    @Column(name = "mp_user_id")
    private String mpUserId;

    @Column(name = "access_token", nullable = false, length = 512)
    private String accessToken;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "linked_at", nullable = false)
    private LocalDateTime linkedAt;
}
