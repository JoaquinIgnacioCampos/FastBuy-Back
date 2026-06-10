package grupo4.fastbuyback.DTOs;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PreferenceRequest(
        @NotEmpty List<ItemDto> items,
        double total,
        String eventId
) {}
