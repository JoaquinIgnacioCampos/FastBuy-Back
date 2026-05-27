package grupo4.fastbuyback.Services;

import grupo4.fastbuyback.Entities.Product;
import grupo4.fastbuyback.Repositories.ProductsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductsService {

    private final ProductsRepository repo;

    public ProductsService(ProductsRepository repo) {
        this.repo = repo;
    }

    public List<Product> getAll() {
        return repo.findAll();
    }
}
