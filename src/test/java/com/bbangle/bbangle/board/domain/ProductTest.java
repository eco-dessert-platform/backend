package com.bbangle.bbangle.board.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] Product 도메인")
class ProductTest {

    @Nested
    @DisplayName("Product 생성자")
    class Constructor {

        private final Board board = BoardFixture.defaultBoard();

        @Test
        @DisplayName("정상적인 입력으로 Product를 생성한다")
        void success() {
            // given & when
            Product product = new Product(
                board, "테스트상품", 1000, "BREAD", 10,
                true, false, true, false, false,
                true, false, false, false, false, false, false,
                new Nutrition(100, 50, 30, 10, 5, 3, 200)
            );

            // then
            assertThat(product.getTitle()).isEqualTo("테스트상품");
            assertThat(product.getPrice()).isEqualTo(1000);
            assertThat(product.getCategory()).isEqualTo(Category.BREAD);
            assertThat(product.getStock()).isEqualTo(10);
            assertThat(product.isGlutenFreeTag()).isTrue();
            assertThat(product.isSugarFreeTag()).isTrue();
            assertThat(product.isMonday()).isTrue();
            assertThat(product.isSoldout()).isFalse();
        }

        @Test
        @DisplayName("title이 null이면 예외가 발생한다")
        void failWhenTitleIsNull() {
            // given & when & then
            assertThatThrownBy(() -> new Product(
                board, null, 1000, "BREAD", 10,
                false, false, false, false, false,
                true, false, false, false, false, false, false,
                null
            ))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PRODUCT_NAME);
        }

        @Test
        @DisplayName("title이 3자 미만이면 예외가 발생한다")
        void failWhenTitleTooShort() {
            // given & when & then
            assertThatThrownBy(() -> new Product(
                board, "ab", 1000, "BREAD", 10,
                false, false, false, false, false,
                true, false, false, false, false, false, false,
                null
            ))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PRODUCT_NAME);
        }

        @Test
        @DisplayName("title이 50자 초과이면 예외가 발생한다")
        void failWhenTitleTooLong() {
            // given
            String longTitle = "가".repeat(51);

            // when & then
            assertThatThrownBy(() -> new Product(
                board, longTitle, 1000, "BREAD", 10,
                false, false, false, false, false,
                true, false, false, false, false, false, false,
                null
            ))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PRODUCT_NAME);
        }

        @Test
        @DisplayName("배송 요일이 하나도 선택되지 않으면 예외가 발생한다")
        void failWhenNoDeliveryDay() {
            // given & when & then
            assertThatThrownBy(() -> new Product(
                board, "테스트상품", 1000, "BREAD", 10,
                false, false, false, false, false,
                false, false, false, false, false, false, false,
                null
            ))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PRODUCT_DELIVERY_DAY);
        }

        @Test
        @DisplayName("유효하지 않은 카테고리이면 예외가 발생한다")
        void failWhenInvalidCategory() {
            // given & when & then
            assertThatThrownBy(() -> new Product(
                board, "테스트상품", 1000, "INVALID", 10,
                false, false, false, false, false,
                true, false, false, false, false, false, false,
                null
            ))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PRODUCT_CATEGORY);
        }
    }

    @Nested
    @DisplayName("update 메서드")
    class Update {

        private final Board board = BoardFixture.defaultBoard();
        private final Nutrition nutrition = new Nutrition(100, 50, 30, 10, 5, 3, 200);

        private Product createDefaultProduct() {
            return new Product(
                board, "기존상품", board.getPrice(), "BREAD", 10,
                true, false, false, false, false,
                true, false, false, false, false, false, false,
                nutrition
            );
        }

        @Test
        @DisplayName("정상적인 입력으로 Product를 수정한다")
        void success() {
            // given
            Product product = createDefaultProduct();
            Nutrition newNutrition = new Nutrition(200, 100, 60, 20, 10, 6, 400);

            // when
            product.update("수정된상품", 500, "COOKIE", 20,
                false, true, false, false, false,
                false, true, false, false, false, false, false,
                newNutrition);

            // then
            assertThat(product.getTitle()).isEqualTo("수정된상품");
            assertThat(product.getPrice()).isEqualTo(500);
            assertThat(product.getCategory()).isEqualTo(Category.COOKIE);
            assertThat(product.getStock()).isEqualTo(20);
            assertThat(product.isSoldout()).isFalse();
            assertThat(product.isGlutenFreeTag()).isFalse();
            assertThat(product.isHighProteinTag()).isTrue();
            assertThat(product.isTuesday()).isTrue();
            assertThat(product.getNutrition()).isEqualTo(newNutrition);
        }

        @Test
        @DisplayName("stock이 0으로 수정되면 soldout이 true가 된다")
        void soldoutWhenStockBecomesZero() {
            // given
            Product product = createDefaultProduct();

            // when
            product.update("기존상품", 0, "BREAD", 0,
                true, false, false, false, false,
                true, false, false, false, false, false, false,
                nutrition);

            // then
            assertThat(product.getStock()).isZero();
            assertThat(product.isSoldout()).isTrue();
        }

        @Test
        @DisplayName("title이 3자 미만이면 예외가 발생한다")
        void failWhenTitleTooShort() {
            // given
            Product product = createDefaultProduct();

            // when & then
            assertThatThrownBy(() -> product.update("ab", 0, "BREAD", 10,
                false, false, false, false, false,
                true, false, false, false, false, false, false,
                nutrition))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PRODUCT_NAME);
        }

        @Test
        @DisplayName("title이 50자 초과이면 예외가 발생한다")
        void failWhenTitleTooLong() {
            // given
            Product product = createDefaultProduct();
            String longTitle = "가".repeat(51);

            // when & then
            assertThatThrownBy(() -> product.update(longTitle, 0, "BREAD", 10,
                false, false, false, false, false,
                true, false, false, false, false, false, false,
                nutrition))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PRODUCT_NAME);
        }

        @Test
        @DisplayName("배송 요일이 하나도 선택되지 않으면 예외가 발생한다")
        void failWhenNoDeliveryDay() {
            // given
            Product product = createDefaultProduct();

            // when & then
            assertThatThrownBy(() -> product.update("기존상품", 0, "BREAD", 10,
                false, false, false, false, false,
                false, false, false, false, false, false, false,
                nutrition))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PRODUCT_DELIVERY_DAY);
        }

        @Test
        @DisplayName("update 호출 시 price는 plusPriceWithBoardPrice 값으로 저장된다")
        void priceStoredAsPlusPriceWithBoardPrice() {
            // given
            Product product = createDefaultProduct();

            // when
            product.update("기존상품", 1000, "BREAD", 10,
                false, false, false, false, false,
                true, false, false, false, false, false, false,
                nutrition);

            // then
            assertThat(product.getPrice()).isEqualTo(1000);
        }
    }

    @Nested
    @DisplayName("getTags 메서드")
    class GetTags {

        @Test
        @DisplayName("활성화된 태그만 반환한다")
        void returnOnlyActiveTags() {
            // given
            Product product = Product.builder()
                .glutenFreeTag(true)
                .highProteinTag(false)
                .sugarFreeTag(true)
                .veganTag(false)
                .ketogenicTag(false)
                .lowFatTag(true)
                .build();

            // when
            var tags = product.getTags();

            // then
            assertThat(tags).containsExactly(
                TagEnum.GLUTEN_FREE.label(),
                TagEnum.SUGAR_FREE.label(),
                TagEnum.LOW_FAT.label()
            );
        }

        @Test
        @DisplayName("활성화된 태그가 없으면 빈 리스트를 반환한다")
        void returnEmptyWhenNoTags() {
            // given
            Product product = Product.builder()
                .glutenFreeTag(false)
                .highProteinTag(false)
                .sugarFreeTag(false)
                .veganTag(false)
                .ketogenicTag(false)
                .lowFatTag(false)
                .build();

            // when
            var tags = product.getTags();

            // then
            assertThat(tags).isEmpty();
        }
    }

    @Nested
    @DisplayName("editStock 메서드")
    class EditStock {

        @Test
        @DisplayName("INCREASE: 재고가 지정된 수량만큼 증가한다")
        void increase_addsStock() {
            // given
            Product product = ProductFixture.createWithStock(null, "테스트상품", 10);

            // when
            product.editStock(5, EditStockFlag.INCREASE);

            // then
            assertThat(product.getStock()).isEqualTo(15);
            assertThat(product.isSoldout()).isFalse();
        }

        @Test
        @DisplayName("INCREASE: 재고가 0인 상태에서도 증가시킬 수 있다")
        void increase_fromZeroStock() {
            // given
            Product product = ProductFixture.createWithStock(null, "테스트상품", 0);

            // when
            product.editStock(10, EditStockFlag.INCREASE);

            // then
            assertThat(product.getStock()).isEqualTo(10);
        }

        @Test
        @DisplayName("DECREASE: 재고가 지정된 수량만큼 감소한다")
        void decrease_subtractsStock() {
            // given
            Product product = ProductFixture.createWithStock(null, "테스트상품", 10);

            // when
            product.editStock(3, EditStockFlag.DECREASE);

            // then
            assertThat(product.getStock()).isEqualTo(7);
            assertThat(product.isSoldout()).isFalse();
        }

        @Test
        @DisplayName("DECREASE: 재고보다 많은 수량을 감소시키면 예외가 발생한다")
        void decrease_throwsExceptionWhenAmountExceedsStock() {
            // given
            Product product = ProductFixture.createWithStock(null, "테스트상품", 5);

            // when & then
            assertThatThrownBy(() -> product.editStock(10, EditStockFlag.DECREASE))
                .isInstanceOf(BbangleException.class);
        }

        @Test
        @DisplayName("DECREASE: 재고와 동일한 수량을 감소시키면 재고가 0이 되고 품절 처리된다")
        void decrease_setsZeroAndSoldoutWhenStockBecomesZero() {
            // given
            Product product = ProductFixture.createWithStock(null, "테스트상품", 5);

            // when
            product.editStock(5, EditStockFlag.DECREASE);

            // then
            assertThat(product.getStock()).isZero();
            assertThat(product.isSoldout()).isTrue();
        }

        @Test
        @DisplayName("SOLDOUT: 재고가 0이 되고 품절 처리된다")
        void soldout_setsZeroStockAndSoldoutTrue() {
            // given
            Product product = ProductFixture.createWithStock(null, "테스트상품", 100);

            // when
            product.editStock(0, EditStockFlag.SOLDOUT);

            // then
            assertThat(product.getStock()).isZero();
            assertThat(product.isSoldout()).isTrue();
        }

        @Test
        @DisplayName("SOLDOUT: 이미 재고가 0인 상품도 품절 처리할 수 있다")
        void soldout_worksWithZeroStock() {
            // given
            Product product = ProductFixture.createWithStock(null, "테스트상품", 0);

            // when
            product.editStock(0, EditStockFlag.SOLDOUT);

            // then
            assertThat(product.getStock()).isZero();
            assertThat(product.isSoldout()).isTrue();
        }
    }
}
