package grupo4.fastbuyback.DTOs;

public record AdminLoginResponse(
    String username,
    String eventId,
    String eventName,
    String token
) {}
