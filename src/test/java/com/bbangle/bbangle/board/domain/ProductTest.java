package com.bbangle.bbangle.board.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] Product 도메인")
class ProductTest {

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
