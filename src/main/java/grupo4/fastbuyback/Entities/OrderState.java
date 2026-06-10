package grupo4.fastbuyback.Entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderState {
    PENDING_PAYMENT,
    QUEUE,
    PREPARING,
    READY,
    DELIVERED;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static OrderState fromJson(String value) {
        return valueOf(value.toUpperCase());
    }
}
