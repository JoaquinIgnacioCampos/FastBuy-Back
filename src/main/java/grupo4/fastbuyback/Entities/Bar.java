package grupo4.fastbuyback.Entities;

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
}
