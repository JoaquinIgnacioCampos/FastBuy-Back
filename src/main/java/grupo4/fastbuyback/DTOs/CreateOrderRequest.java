package grupo4.fastbuyback.DTOs;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Order-creation payload. {@code bar} is optional — when absent, the server
 * assigns the least-loaded bar that can serve every requested item.
 */
public record CreateOrderRequest(
        @NotEmpty List<ItemDto> items,
        String bar,
        double total
) {}
