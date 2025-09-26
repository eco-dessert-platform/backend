package com.bbangle.bbangle.seller.store.order.controller;


public class OrderResponse {

    public record OrderSearchResponse(
        String orderStatus,
        String deliveryStatus,
        String courierCompany,
        String trackingNumber,
        String orderNumber,
        String paymentAt,
        String recipientName,
        String itemName
    ) {

    }

}
