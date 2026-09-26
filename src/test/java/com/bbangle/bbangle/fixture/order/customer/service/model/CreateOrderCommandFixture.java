package com.bbangle.bbangle.fixture.order.customer.service.model;

import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand.OptionOrder;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand.Orderer;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand.PaymentAmount;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand.ProductOrder;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand.ShippingAddress;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand.StoreOrder;
import com.bbangle.bbangle.payment.domain.PaymentMethod;
import java.util.List;

public final class CreateOrderCommandFixture {

    public static final String DEFAULT_TRANSACTION_ID = "550e8400-e29b-41d4-a716-446655440000";

    private CreateOrderCommandFixture() {
    }

    public static Orderer defaultOrderer() {
        return new Orderer("홍길동", "01012345678", "buyer@example.com");
    }

    public static ShippingAddress defaultShippingAddress() {
        return new ShippingAddress(
            "홍길동",
            "01012345678",
            "13529",
            "경기도 성남시 분당구 판교역로 166",
            "101동 1001호",
            "문 앞에 놔주세요",
            true);
    }

    /** 옵션 1건만 담긴 스토어. */
    public static StoreOrder store(Long storeId, int deliveryFee, Long productId, OptionOrder option) {
        return new StoreOrder(storeId, deliveryFee, List.of(new ProductOrder(productId, List.of(option))));
    }

    public static OptionOrder option(Long optionId, int quantity, int price) {
        return new OptionOrder(optionId, quantity, price);
    }

    /**
     * 상품금액·최종금액을 직접 지정한다. 할인이 없는 기본 케이스에서는
     * {@code totalAmount = productAmount + deliveryFee} 가 된다.
     */
    public static PaymentAmount amount(int productAmount, int discountAmount, int deliveryFee) {
        return new PaymentAmount(
            productAmount, discountAmount, deliveryFee,
            productAmount - discountAmount + deliveryFee);
    }

    public static CreateOrderCommand.CreateOrderCommandBuilder defaultCommand() {
        return CreateOrderCommand.builder()
            .transactionId(DEFAULT_TRANSACTION_ID)
            .orderer(defaultOrderer())
            .shippingAddress(defaultShippingAddress())
            .paymentMethod(PaymentMethod.CARD)
            .privacyAgreed(true);
    }

    public static CreateOrderCommand create(
        Long memberId, List<StoreOrder> stores, PaymentAmount paymentAmount
    ) {
        return defaultCommand()
            .memberId(memberId)
            .stores(stores)
            .paymentAmount(paymentAmount)
            .build();
    }

    /** 스토어 1개 · 상품 1개 · 옵션 1개짜리 최소 주문. */
    public static CreateOrderCommand createSingle(
        Long memberId, Long storeId, Long productId, Long optionId,
        int quantity, int unitPrice, int deliveryFee
    ) {
        return create(
            memberId,
            List.of(store(storeId, deliveryFee, productId, option(optionId, quantity, unitPrice))),
            amount(unitPrice * quantity, 0, deliveryFee));
    }
}
