package com.bbangle.bbangle.board.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.ProductInfoNoticeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] ProductInfoNotice 도메인")
class ProductInfoNoticeTest {

    @Nested
    @DisplayName("update 메서드")
    class Update {

        @Test
        @DisplayName("정상적인 입력으로 ProductInfoNotice를 수정한다")
        void success() {
            // given
            ProductInfoNotice notice = ProductInfoNoticeFixture.defaultNotice();

            // when
            notice.update("수정된상품명", "과자류", "수정제조사", "수정소재지",
                "2025-01-01", "2025-12-31", "냉장보관",
                "1개", "밀가루, 설탕", "탄수화물 30g",
                "해당없음", "알레르기 주의", "해당없음");

            // then
            assertThat(notice.getProductName()).isEqualTo("수정된상품명");
            assertThat(notice.getFoodType()).isEqualTo("과자류");
            assertThat(notice.getManufacturer()).isEqualTo("수정제조사");
            assertThat(notice.getOriginLocation()).isEqualTo("수정소재지");
            assertThat(notice.getManufactureDate()).isEqualTo("2025-01-01");
            assertThat(notice.getExpirationDate()).isEqualTo("2025-12-31");
            assertThat(notice.getStorageGuide()).isEqualTo("냉장보관");
            assertThat(notice.getPackagingQuantityUnit()).isEqualTo("1개");
            assertThat(notice.getRawMaterialName()).isEqualTo("밀가루, 설탕");
            assertThat(notice.getNutritionInfo()).isEqualTo("탄수화물 30g");
            assertThat(notice.getTransgenic()).isEqualTo("해당없음");
            assertThat(notice.getCustomerWarning()).isEqualTo("알레르기 주의");
            assertThat(notice.getImportFood()).isEqualTo("해당없음");
        }

        @Test
        @DisplayName("productName이 null이면 예외가 발생한다")
        void failWhenProductNameIsNull() {
            // given
            ProductInfoNotice notice = ProductInfoNoticeFixture.defaultNotice();

            // when & then
            assertThatThrownBy(() -> notice.update(null, null, null, null,
                null, null, null, null, null, null, null, null, null))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PRODUCT_INFO_NOTICE_NAME);
        }

        @Test
        @DisplayName("productName이 3자 미만이면 예외가 발생한다")
        void failWhenProductNameTooShort() {
            // given
            ProductInfoNotice notice = ProductInfoNoticeFixture.defaultNotice();

            // when & then
            assertThatThrownBy(() -> notice.update("ab", null, null, null,
                null, null, null, null, null, null, null, null, null))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PRODUCT_INFO_NOTICE_NAME);
        }

        @Test
        @DisplayName("productName이 50자 초과이면 예외가 발생한다")
        void failWhenProductNameTooLong() {
            // given
            ProductInfoNotice notice = ProductInfoNoticeFixture.defaultNotice();
            String longName = "가".repeat(51);

            // when & then
            assertThatThrownBy(() -> notice.update(longName, null, null, null,
                null, null, null, null, null, null, null, null, null))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PRODUCT_INFO_NOTICE_NAME);
        }
    }
}
