package grupo4.fastbuyback.Repositories;

import grupo4.fastbuyback.Entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductsRepository extends JpaRepository<Product, String> {

    @Query(value = """
        SELECT p.* FROM products p
        JOIN bar_products bp ON bp.product_id = p.id
        WHERE bp.bar_id = :barId
        """, nativeQuery = true)
    List<Product> findByBarId(@Param("barId") String barId);

    /**
     * Atomically decrements stock only if enough is available. Returns the number
     * of rows updated (1 = reserved, 0 = insufficient stock / unknown product),
     * which prevents overselling under concurrency.
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :q WHERE p.id = :id AND p.stock >= :q")
    int decrementStock(@Param("id") String id, @Param("q") int q);
}
