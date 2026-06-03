package grupo4.fastbuyback.Services;

import grupo4.fastbuyback.Entities.Bar;
import grupo4.fastbuyback.Entities.Product;
import grupo4.fastbuyback.Repositories.BarsRepository;
import grupo4.fastbuyback.Repositories.ProductsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public List<Bar> getByEvent(String eventId) {
        return repo.findByEventId(eventId);
    }

    public List<Product> getMenuForBar(String barId) {
        if (!repo.existsById(barId)) {
            throw new IllegalArgumentException("Bar not found: " + barId);
        }
        return productsRepo.findByBarId(barId);
    }

    /** Returns the distinct union of products served across all bars of an event. */
    public List<Product> getMenuForEvent(String eventId) {
        List<Bar> bars = repo.findByEventId(eventId);
        Map<String, Product> seen = new LinkedHashMap<>();
        for (Bar bar : bars) {
            for (Product p : productsRepo.findByBarId(bar.getId())) {
                seen.putIfAbsent(p.getId(), p);
            }
        }
        return new ArrayList<>(seen.values());
    }
}
