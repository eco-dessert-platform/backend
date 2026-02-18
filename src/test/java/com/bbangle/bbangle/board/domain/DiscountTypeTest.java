package com.bbangle.bbangle.board.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] DiscountType 도메인")
class DiscountTypeTest {

    @Nested
    @DisplayName("from 메서드")
    class From {

        @Test
        @DisplayName("RATE 문자열로 RATE 타입을 반환한다")
        void returnRate() {
            // given & when
            DiscountType result = DiscountType.from("RATE");

            // then
            assertThat(result).isEqualTo(DiscountType.RATE);
        }

        @Test
        @DisplayName("AMOUNT 문자열로 AMOUNT 타입을 반환한다")
        void returnAmount() {
            // given & when
            DiscountType result = DiscountType.from("AMOUNT");

            // then
            assertThat(result).isEqualTo(DiscountType.AMOUNT);
        }

        @Test
        @DisplayName("유효하지 않은 문자열이면 예외가 발생한다")
        void failWhenInvalidValue() {
            // given & when & then
            assertThatThrownBy(() -> DiscountType.from("INVALID"))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_DISCOUNT_TYPE);
        }

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void failWhenNull() {
            // given & when & then
            assertThatThrownBy(() -> DiscountType.from(null))
                .isInstanceOf(Exception.class);
        }
    }
}
