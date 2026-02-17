package com.bbangle.bbangle.board.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] Category 도메인")
class CategoryTest {

    @Nested
    @DisplayName("from 메서드")
    class From {

        @Test
        @DisplayName("BREAD 문자열로 BREAD 카테고리를 반환한다")
        void returnBread() {
            // given & when
            Category result = Category.from("BREAD");

            // then
            assertThat(result).isEqualTo(Category.BREAD);
            assertThat(result.getMainCategory()).isEqualTo(MainCategory.BREAD);
        }

        @Test
        @DisplayName("COOKIE 문자열로 COOKIE 카테고리를 반환한다")
        void returnCookie() {
            // given & when
            Category result = Category.from("COOKIE");

            // then
            assertThat(result).isEqualTo(Category.COOKIE);
            assertThat(result.getMainCategory()).isEqualTo(MainCategory.SNACK);
        }

        @Test
        @DisplayName("ETC 문자열로 ETC 카테고리를 반환한다")
        void returnEtc() {
            // given & when
            Category result = Category.from("ETC");

            // then
            assertThat(result).isEqualTo(Category.ETC);
            assertThat(result.getMainCategory()).isNull();
        }

        @Test
        @DisplayName("유효하지 않은 문자열이면 예외가 발생한다")
        void failWhenInvalidValue() {
            // given & when & then
            assertThatThrownBy(() -> Category.from("INVALID"))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PRODUCT_CATEGORY);
        }
    }
}
