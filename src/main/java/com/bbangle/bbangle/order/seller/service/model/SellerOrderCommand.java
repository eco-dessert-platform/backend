package com.bbangle.bbangle.order.seller.service.model;

import java.util.List;
import lombok.Builder;

public class SellerOrderCommand {

    @Builder
    public record OrderConfirmCommand(
        Long orderId,
        List<Long> orderItemIds,
        Long sellerId
    ) {
    }

    @Builder
    public record ShipmentRegisterCommand(
        Long orderId,
        List<Long> orderItemIds,
        String courierName,
        String trackingNumber,
        Long sellerId
    ) {
    }

    @Builder
    public record ShipmentModifyCommand(
        Long orderId,
        List<Long> orderItemIds,
        String courierName,
        String trackingNumber,
        Long sellerId
    ) {
    }
}
