package com.bbangle.bbangle.order.service.model;

import java.util.List;
import lombok.Builder;

public class SellerOrderCommand {

    @Builder
    public record OrderConfirmCommand(
        Long sellerId,
        Long orderId,
        List<Long> orderItemIds
    ) {
    }
}
