package grupo4.fastbuyback.Repositories;

import grupo4.fastbuyback.Entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriesRepository extends JpaRepository<Category, String> {}
