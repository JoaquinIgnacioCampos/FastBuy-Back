package grupo4.fastbuyback.DTOs;

import jakarta.validation.constraints.NotBlank;

public record LinkAccountRequest(
    @NotBlank String accessToken
) {}
