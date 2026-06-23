package grupo4.fastbuyback.DTOs;

public record LoginResponse(
    String username,
    String barId,
    String barLabel,
    String eventId,
    String eventName,
    String token
) {}
