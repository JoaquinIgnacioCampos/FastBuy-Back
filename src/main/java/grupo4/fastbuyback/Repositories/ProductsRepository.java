package grupo4.fastbuyback.Repositories;

import grupo4.fastbuyback.Entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository<Product, String> {}
