package com.bbangle.bbangle.board.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] DeliveryType 도메인")
class DeliveryTypeTest {

    @Nested
    @DisplayName("from 팩토리 메서드")
    class From {

        @Test
        @DisplayName("deliveryFee가 null이면 FREE를 반환한다")
        void returnsFreeWhenDeliveryFeeIsNull() {
            // given & when
            DeliveryType result = DeliveryType.from(null, null);

            // then
            assertThat(result).isEqualTo(DeliveryType.FREE);
        }

        @Test
        @DisplayName("deliveryFee가 0이면 FREE를 반환한다")
        void returnsFreeWhenDeliveryFeeIsZero() {
            // given & when
            DeliveryType result = DeliveryType.from(0, null);

            // then
            assertThat(result).isEqualTo(DeliveryType.FREE);
        }

        @Test
        @DisplayName("deliveryFee가 0이고 freeShippingConditions가 있어도 FREE를 반환한다")
        void returnsFreeWhenDeliveryFeeIsZeroWithConditions() {
            // given & when
            DeliveryType result = DeliveryType.from(0, 30000);

            // then
            assertThat(result).isEqualTo(DeliveryType.FREE);
        }

        @Test
        @DisplayName("deliveryFee가 양수이고 freeShippingConditions가 있으면 CONDITIONAL_FREE를 반환한다")
        void returnsConditionalFreeWhenFeeExistsWithConditions() {
            // given & when
            DeliveryType result = DeliveryType.from(3000, 30000);

            // then
            assertThat(result).isEqualTo(DeliveryType.CONDITIONAL_FREE);
        }

        @Test
        @DisplayName("deliveryFee가 양수이고 freeShippingConditions가 null이면 PAID를 반환한다")
        void returnsPaidWhenFeeExistsWithoutConditions() {
            // given & when
            DeliveryType result = DeliveryType.from(3000, null);

            // then
            assertThat(result).isEqualTo(DeliveryType.PAID);
        }
    }
}
