package grupo4.fastbuyback.DTOs;

import java.time.LocalDateTime;

public record PaymentAccountResponse(
    boolean linked,
    String mpUserId,
    String mpEmail,
    LocalDateTime linkedAt
) {
    public static PaymentAccountResponse unlinked() {
        return new PaymentAccountResponse(false, null, null, null);
    }
}
