package com.bbangle.bbangle.order.customer.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import java.util.List;
import java.util.OptionalLong;

/**
 * 주문 금액 계산기. DB 에 의존하지 않는 순수 계산만 담는다.
 *
 * <p>금액은 전부 서버가 계산하며 클라이언트가 보낸 금액은 검증용으로만 쓴다.
 *
 * <p><b>확정되지 않은 정책(기획 확인 대기)</b> — 아래 3가지는 추천안을 가정으로 구현했다.
 * 뒤집을 때 고쳐야 할 지점은 이 클래스 안에 전부 모여 있다.
 * <ul>
 *   <li>① 옵션 추가금에는 할인을 적용하지 않는다 → {@link OrderLine#unitPrice()}</li>
 *   <li>② 무료배송 조건은 '할인 적용 후' 상품금액 기준으로 판정한다 → {@link #calculateDeliveryFee}</li>
 *   <li>③ 스토어 내 게시글별 배송비가 다르면 가장 비싼 값을 적용한다 → {@link #calculateDeliveryFee}</li>
 * </ul>
 */
public final class OrderAmountCalculator {

    private OrderAmountCalculator() {
    }

    /**
     * 주문 한 줄. 게시글(상품)과 그 옵션, 수량을 묶는다.
     */
    public record OrderLine(Product option, int quantity) {

        public Board board() {
            return option.getBoard();
        }

        /** 정가 단가. 옵션 가격은 게시글 가격에 더해지는 '추가금'이다. */
        public int productPrice() {
            return board().getPrice() + option.getPrice();
        }

        /**
         * 할인 적용 단가.
         *
         * <p>① 옵션 추가금에는 할인을 적용하지 않는다.
         * {@code board.discountPrice} 는 게시글 정가 기준으로 미리 계산되어 저장된 값이다.
         */
        public int unitPrice() {
            return discountedBoardPrice() + option.getPrice();
        }

        public long totalPrice() {
            return (long) unitPrice() * quantity;
        }

        /**
         * 게시글의 할인 적용가.
         *
         * <p>{@code discount_price} 는 판매자 등록·수정 경로에서만 채워지므로 그 외 경로로 들어온
         * 데이터는 0 일 수 있다. 0 이거나 정가를 넘는 값은 할인이 없는 것으로 보고 정가를 쓴다.
         * (그대로 쓰면 게시글 금액이 0 원이 되어 과소 청구된다)
         */
        private int discountedBoardPrice() {
            int price = board().getPrice();
            int discounted = board().getDiscountPrice();
            if (discounted <= 0 || discounted > price) {
                return price;
            }
            return discounted;
        }
    }

    /**
     * 스토어(=주문) 단위 금액.
     */
    public record StoreAmount(
        long productAmount,
        long discountAmount,
        long deliveryFee,
        long totalAmount
    ) {
    }

    public static StoreAmount calculate(List<OrderLine> lines) {
        long productAmount = lines.stream()
            .mapToLong(line -> (long) line.productPrice() * line.quantity())
            .sum();

        long discountAmount = lines.stream()
            .mapToLong(line -> (long) (line.productPrice() - line.unitPrice()) * line.quantity())
            .sum();

        long payableAmount = productAmount - discountAmount;
        long deliveryFee = calculateDeliveryFee(lines, payableAmount);

        return new StoreAmount(productAmount, discountAmount, deliveryFee, payableAmount + deliveryFee);
    }

    /**
     * ③ 스토어 내 게시글별 배송비가 다르면 가장 비싼 값을 적용한다.
     * ② 무료배송 조건(게시글별 조건 중 최솟값)을 '할인 적용 후' 상품금액이 충족하면 0 원이다.
     */
    private static long calculateDeliveryFee(List<OrderLine> lines, long payableAmount) {
        long fee = lines.stream()
            .mapToLong(line -> nullSafe(line.board().getDeliveryFee()))
            .max()
            .orElse(0L);

        OptionalLong threshold = lines.stream()
            .map(line -> line.board().getFreeShippingConditions())
            .filter(condition -> condition != null && condition > 0)
            .mapToLong(Integer::longValue)
            .min();

        if (threshold.isPresent() && payableAmount >= threshold.getAsLong()) {
            return 0L;
        }
        return fee;
    }

    private static long nullSafe(Integer value) {
        return value != null ? value : 0L;
    }
}
