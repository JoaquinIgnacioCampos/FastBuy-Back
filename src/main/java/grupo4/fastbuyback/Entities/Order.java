package grupo4.fastbuyback.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double total;

    @Column(columnDefinition = "VARCHAR(2000)")
    private String items;   // stored as JSON string: [{"pid":"p1","q":2}]

    @Enumerated(EnumType.STRING)
    private OrderState status;

    private String bar;

    private String time;

    @Column(name = "claimed_by")
    private String claimedBy;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;
}
