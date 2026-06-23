package grupo4.fastbuyback.DTOs;

import grupo4.fastbuyback.Entities.OrderState;

import java.util.List;

public record OrderResponse(
        String id,
        double total,
        List<ItemDto> items,
        OrderState status,
        String bar,
        String barLabel,
        String time,
        String claimedBy,
        Integer queuePosition,
        Integer etaMinutes
) {}
