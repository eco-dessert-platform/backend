package com.bbangle.bbangle.board.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] ProductionStartTime 도메인")
class ProductionStartTimeTest {

    @Nested
    @DisplayName("from 메서드")
    class From {

        @Test
        @DisplayName("T_09_10 문자열로 09시 시작 시간을 반환한다")
        void returnT0910() {
            // given & when
            ProductionStartTime result = ProductionStartTime.from("T_09_10");

            // then
            assertThat(result).isEqualTo(ProductionStartTime.T_09_10);
            assertThat(result.getLabel()).isEqualTo("09:00~10:00");
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        }

        @Test
        @DisplayName("T_00_01 문자열로 자정 시작 시간을 반환한다")
        void returnT0001() {
            // given & when
            ProductionStartTime result = ProductionStartTime.from("T_00_01");

            // then
            assertThat(result).isEqualTo(ProductionStartTime.T_00_01);
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(0, 0));
        }

        @Test
        @DisplayName("유효하지 않은 문자열이면 예외가 발생한다")
        void failWhenInvalidValue() {
            // given & when & then
            assertThatThrownBy(() -> ProductionStartTime.from("INVALID"))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PRODUCTION_START_TIME);
        }
    }
}
