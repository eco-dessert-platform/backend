package com.bbangle.bbangle.order.customer.service.model;

import com.bbangle.bbangle.order.customer.controller.dto.request.CreateOrderRequest;
import com.bbangle.bbangle.payment.domain.PaymentMethod;
import java.util.List;
import lombok.Builder;

@Builder
public record CreateOrderCommand(
    Long memberId,
    String transactionId,
    List<StoreOrder> stores,
    Orderer orderer,
    ShippingAddress shippingAddress,
    PaymentMethod paymentMethod,
    boolean privacyAgreed,
    PaymentAmount paymentAmount
) {

    public record StoreOrder(Long storeId, int deliveryFee, List<ProductOrder> products) {
    }

    public record ProductOrder(Long productId, List<OptionOrder> options) {
    }

    /** {@code price} 는 옵션 1개당 할인 적용 단가다. 서버 계산값과 대조한다. */
    public record OptionOrder(Long optionId, int quantity, int price) {
    }

    public record Orderer(String name, String phone, String email) {
    }

    public record ShippingAddress(
        String recipientName,
        String recipientPhone,
        String zipCode,
        String address,
        String addressDetail,
        String deliveryMemo,
        boolean saveAsDefault
    ) {
    }

    public record PaymentAmount(
        int productAmount,
        int discountAmount,
        int deliveryFee,
        int totalAmount
    ) {
    }

    public static CreateOrderCommand of(Long memberId, String transactionId, CreateOrderRequest request) {
        return CreateOrderCommand.builder()
            .memberId(memberId)
            .transactionId(transactionId)
            .stores(request.stores().stream()
                .map(store -> new StoreOrder(
                    store.storeId(),
                    store.deliveryFee(),
                    store.products().stream()
                        .map(product -> new ProductOrder(
                            product.productId(),
                            product.options().stream()
                                .map(option -> new OptionOrder(
                                    option.optionId(), option.quantity(), option.price()))
                                .toList()))
                        .toList()))
                .toList())
            .orderer(new Orderer(
                request.orderer().name(),
                request.orderer().phone(),
                request.orderer().email()))
            .shippingAddress(new ShippingAddress(
                request.shippingAddress().recipientName(),
                request.shippingAddress().recipientPhone(),
                request.shippingAddress().zipCode(),
                request.shippingAddress().address(),
                request.shippingAddress().addressDetail(),
                request.shippingAddress().deliveryMemo(),
                request.shippingAddress().saveAsDefault()))
            .paymentMethod(request.paymentMethod())
            .privacyAgreed(request.privacyAgreed())
            .paymentAmount(new PaymentAmount(
                request.paymentAmount().productAmount(),
                request.paymentAmount().discountAmount(),
                request.paymentAmount().deliveryFee(),
                request.paymentAmount().totalAmount()))
            .build();
    }

    /** 요청에 담긴 모든 옵션을 평탄화한다. 중복 검사·일괄 조회에 쓴다. */
    public List<OptionOrder> allOptions() {
        return stores.stream()
            .flatMap(store -> store.products().stream())
            .flatMap(product -> product.options().stream())
            .toList();
    }
}
