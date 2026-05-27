package grupo4.fastbuyback.Services;

import grupo4.fastbuyback.Entities.Category;
import grupo4.fastbuyback.Repositories.CategoriesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriesService {

    private final CategoriesRepository repo;

    public CategoriesService(CategoriesRepository repo) {
        this.repo = repo;
    }

    public List<Category> getAll() {
        return repo.findAll();
    }
}
