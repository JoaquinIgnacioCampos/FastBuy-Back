package grupo4.fastbuyback.DTOs;

import jakarta.validation.constraints.NotBlank;

public record PreferenceRequest(
        @NotBlank String orderId
) {}
