package grupo4.fastbuyback.Controllers;

import grupo4.fastbuyback.Entities.Product;
import grupo4.fastbuyback.Services.ProductsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final ProductsService service;

    public ProductsController(ProductsService service) {
        this.service = service;
    }

    @GetMapping
    public List<Product> getAll() {
        return service.getAll();
    }
}
