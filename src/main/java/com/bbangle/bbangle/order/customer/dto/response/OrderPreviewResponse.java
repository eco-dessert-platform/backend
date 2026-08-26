package com.bbangle.bbangle.order.customer.dto.response;

import java.util.List;

public record OrderPreviewResponse(
    List<StoreOrderGroup> stores,
    DeliveryAddressInfo deliveryAddress,
    int totalProductAmount,
    int totalDeliveryFee,
    int totalPaymentAmount
) {

    public record StoreOrderGroup(
        Long storeId,
        String storeName,
        List<OrderItemInfo> items,
        int storeSubtotal,
        int deliveryFee
    ) {}

    public record OrderItemInfo(
        Long productId,
        String productName,
        String thumbnailUrl,
        int quantity,
        int unitPrice,
        int totalPrice
    ) {}

    public record DeliveryAddressInfo(
        Long id,
        String addressName,
        String recipientName,
        String phone,
        String address,
        String addressDetail,
        String zipCode,
        boolean isDefault
    ) {}

}
