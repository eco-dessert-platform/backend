package com.bbangle.bbangle.order.seller.controller.model;

public record PaymentInfo(

    String paymentStatus,
    String paymentMethod
) {
    public static PaymentInfo of(String paymentStatus, String paymentMethod) {
        return new PaymentInfo(paymentStatus, paymentMethod);


    }


}
