package grupo4.fastbuyback.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    private String id;

    private String name;

    private String category;

    private double price;

    private int stock;

    /** Display image: either a URL (starts with "/" or "http") or an emoji string. */
    @Column(columnDefinition = "VARCHAR(255)")
    private String image;

    /** Fallback emoji used when {@link #image} is empty or fails to render. */
    @Column(length = 16)
    private String emoji;

    /** Short descriptor shown under the product name (e.g. "Pinta · 473 ml"). */
    private String subtitle;
}
