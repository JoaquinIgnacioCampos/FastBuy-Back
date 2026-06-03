package grupo4.fastbuyback.Services;

import grupo4.fastbuyback.Entities.Bar;
import grupo4.fastbuyback.Entities.Product;
import grupo4.fastbuyback.Repositories.BarsRepository;
import grupo4.fastbuyback.Repositories.ProductsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BarsService {

    private final BarsRepository repo;
    private final ProductsRepository productsRepo;

    public BarsService(BarsRepository repo, ProductsRepository productsRepo) {
        this.repo = repo;
        this.productsRepo = productsRepo;
    }

    public List<Bar> getAll() {
        return repo.findAll();
    }

    public List<Product> getMenuForBar(String barId) {
        if (!repo.existsById(barId)) {
            throw new IllegalArgumentException("Bar not found: " + barId);
        }
        return productsRepo.findByBarId(barId);
    }
}
