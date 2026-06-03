package grupo4.fastbuyback.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bars")
@Getter
@Setter
@NoArgsConstructor
public class Bar {

    @Id
    private String id;

    private String label;

    private String location;

    @Column(name = "event_id", insertable = false, updatable = false)
    private String eventId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
