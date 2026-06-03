package grupo4.fastbuyback.Repositories;

import grupo4.fastbuyback.Entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
