package com.bbangle.bbangle.order.customer.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.order.customer.service.OrderAmountCalculator.OrderLine;
import com.bbangle.bbangle.order.customer.service.OrderAmountCalculator.StoreAmount;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 주문 금액 계산 규칙을 고정하는 테스트.
 *
 * <p>기획 확인 대기 중인 정책 3가지(① 옵션 할인 ② 무료배송 기준 ③ 스토어 내 배송비)를
 * 추천안대로 고정한다. 정책이 뒤집히면 이 테스트부터 바뀐다.
 */
@DisplayName("[단위테스트] OrderAmountCalculator")
class OrderAmountCalculatorTest {

    private static Board board(int price, int discountPrice, Integer deliveryFee, Integer freeShippingConditions) {
        return Board.builder()
            .title("상품")
            .price(price)
            .discountPrice(discountPrice)
            .deliveryFee(deliveryFee)
            .freeShippingConditions(freeShippingConditions)
            .saleStatus(SaleStatus.ON_SALE)
            .build();
    }

    private static OrderLine line(Board board, int optionPrice, int quantity) {
        Product option = Product.builder()
            .board(board)
            .title("옵션")
            .price(optionPrice)
            .build();
        return new OrderLine(option, quantity);
    }

    @Nested
    @DisplayName("단가 계산")
    class UnitPrice {

        @DisplayName("옵션 추가금은 게시글 가격에 더해지고, 옵션에는 할인이 적용되지 않는다")
        @Test
        void optionPriceIsAddedWithoutDiscount() {
            // given : 10,000원 상품 20% 할인(할인가 8,000) + 2,000원 옵션 1개
            Board board = board(10_000, 8_000, 0, null);

            // when
            StoreAmount amount = OrderAmountCalculator.calculate(List.of(line(board, 2_000, 1)));

            // then : 정가 12,000 / 할인가 10,000 → 할인액은 게시글 몫 2,000 뿐이다
            assertThat(amount.productAmount()).isEqualTo(12_000);
            assertThat(amount.discountAmount()).isEqualTo(2_000);
            assertThat(amount.totalAmount()).isEqualTo(10_000);
        }

        @DisplayName("수량은 단가에 곱해진다")
        @Test
        void quantityIsMultiplied() {
            // given
            Board board = board(10_000, 8_000, 0, null);

            // when
            StoreAmount amount = OrderAmountCalculator.calculate(List.of(line(board, 2_000, 3)));

            // then
            assertThat(amount.productAmount()).isEqualTo(36_000);
            assertThat(amount.discountAmount()).isEqualTo(6_000);
            assertThat(amount.totalAmount()).isEqualTo(30_000);
        }

        @DisplayName("discountPrice 가 0이면 할인이 없는 것으로 보고 정가를 청구한다")
        @Test
        void treatsZeroDiscountPriceAsNoDiscount() {
            // given : discount_price 가 채워지지 않은 레거시 게시글
            Board board = board(10_000, 0, 0, null);

            // when
            StoreAmount amount = OrderAmountCalculator.calculate(List.of(line(board, 2_000, 1)));

            // then : 게시글 금액이 0원이 되어 과소 청구되면 안 된다
            assertThat(amount.discountAmount()).isZero();
            assertThat(amount.totalAmount()).isEqualTo(12_000);
        }

        @DisplayName("discountPrice 가 정가보다 크면 할인이 없는 것으로 본다")
        @Test
        void treatsInvalidDiscountPriceAsNoDiscount() {
            // given
            Board board = board(10_000, 15_000, 0, null);

            // when
            StoreAmount amount = OrderAmountCalculator.calculate(List.of(line(board, 0, 1)));

            // then
            assertThat(amount.discountAmount()).isZero();
            assertThat(amount.totalAmount()).isEqualTo(10_000);
        }
    }

    @Nested
    @DisplayName("배송비 계산")
    class DeliveryFee {

        @DisplayName("스토어 안에서 게시글별 배송비가 다르면 가장 비싼 값을 적용한다")
        @Test
        void usesMaxDeliveryFeeInStore() {
            // given : 배송비 3,000원 상품 + 2,500원 상품
            Board expensive = board(10_000, 0, 3_000, null);
            Board cheap = board(5_000, 0, 2_500, null);

            // when
            StoreAmount amount = OrderAmountCalculator.calculate(
                List.of(line(expensive, 0, 1), line(cheap, 0, 1)));

            // then
            assertThat(amount.deliveryFee()).isEqualTo(3_000);
            assertThat(amount.totalAmount()).isEqualTo(18_000);
        }

        @DisplayName("배송비가 지정되지 않으면 0원이다")
        @Test
        void nullDeliveryFeeIsFree() {
            // given
            Board board = board(10_000, 0, null, null);

            // when
            StoreAmount amount = OrderAmountCalculator.calculate(List.of(line(board, 0, 1)));

            // then
            assertThat(amount.deliveryFee()).isZero();
        }

        @DisplayName("무료배송 조건을 충족하면 배송비가 0원이 된다")
        @Test
        void freeShippingWhenThresholdMet() {
            // given : 30,000원 이상 무료배송, 상품금액 30,000원
            Board board = board(30_000, 0, 3_000, 30_000);

            // when
            StoreAmount amount = OrderAmountCalculator.calculate(List.of(line(board, 0, 1)));

            // then
            assertThat(amount.deliveryFee()).isZero();
            assertThat(amount.totalAmount()).isEqualTo(30_000);
        }

        @DisplayName("무료배송 판정은 할인 적용 후 금액 기준이다")
        @Test
        void freeShippingIsJudgedAfterDiscount() {
            // given : 정가 32,000 → 할인가 28,800, 무료배송 조건 30,000원
            Board board = board(32_000, 28_800, 3_000, 30_000);

            // when
            StoreAmount amount = OrderAmountCalculator.calculate(List.of(line(board, 0, 1)));

            // then : 할인 전(32,000)이면 무료지만 할인 후(28,800)라 조건 미달이다
            assertThat(amount.deliveryFee()).isEqualTo(3_000);
            assertThat(amount.totalAmount()).isEqualTo(31_800);
        }

        @DisplayName("무료배송 조건이 여러 개면 가장 낮은 조건을 적용한다")
        @Test
        void usesLowestFreeShippingThreshold() {
            // given : 조건 50,000원 상품 + 조건 20,000원 상품, 상품금액 25,000원
            Board strict = board(20_000, 0, 3_000, 50_000);
            Board loose = board(5_000, 0, 2_000, 20_000);

            // when
            StoreAmount amount = OrderAmountCalculator.calculate(
                List.of(line(strict, 0, 1), line(loose, 0, 1)));

            // then
            assertThat(amount.deliveryFee()).isZero();
            assertThat(amount.totalAmount()).isEqualTo(25_000);
        }
    }
}
