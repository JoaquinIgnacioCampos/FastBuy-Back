package grupo4.fastbuyback.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty List<ItemDto> items,
        @NotBlank String bar,
        double total
) {}
