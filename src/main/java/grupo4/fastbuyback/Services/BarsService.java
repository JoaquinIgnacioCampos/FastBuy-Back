package grupo4.fastbuyback.Services;

import grupo4.fastbuyback.Entities.Bar;
import grupo4.fastbuyback.Repositories.BarsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BarsService {

    private final BarsRepository repo;

    public BarsService(BarsRepository repo) {
        this.repo = repo;
    }

    public List<Bar> getAll() {
        return repo.findAll();
    }
}
