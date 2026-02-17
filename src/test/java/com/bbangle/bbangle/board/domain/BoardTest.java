package com.bbangle.bbangle.board.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.store.domain.Store;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] Board 도메인")
class BoardTest {

    private final Store store = StoreFixture.defaultStore();
    private final ProductInfoNotice productInfoNotice = ProductInfoNotice.builder()
        .productName("테스트상품명")
        .build();
    private final BoardDetail boardDetail = BoardDetail.builder()
        .content("<p>상세 내용</p>")
        .build();

    @Nested
    @DisplayName("sellerCreate 메서드")
    class SellerCreate {

        @Test
        @DisplayName("정상적인 입력으로 Board를 생성한다")
        void success() {
            // given
            List<ProductImg> productImgs = List.of(
                ProductImg.builder().url("img1.png").imgOrder(0).build()
            );

            // when
            Board board = Board.sellerCreate(
                store, "테스트 게시글", 10000, "RATE", 10,
                3000, 30000, false, "T_09_10", "NORMAL",
                "CJ대한통운", productInfoNotice, boardDetail, productImgs
            );

            // then
            assertThat(board.getStore()).isEqualTo(store);
            assertThat(board.getTitle()).isEqualTo("테스트 게시글");
            assertThat(board.getPrice()).isEqualTo(10000);
            assertThat(board.getDiscountType()).isEqualTo(DiscountType.RATE);
            assertThat(board.getDiscountValue()).isEqualTo(10);
            assertThat(board.getDeliveryFee()).isEqualTo(3000);
            assertThat(board.getFreeShippingConditions()).isEqualTo(30000);
            assertThat(board.getIsFresh()).isFalse();
            assertThat(board.getProductionStartTime()).isEqualTo(ProductionStartTime.T_09_10);
            assertThat(board.getDeliveryCondition()).isEqualTo("NORMAL");
            assertThat(board.getCourier()).isEqualTo("CJ대한통운");
            assertThat(board.getStatus()).isFalse();
            assertThat(board.getProductImgs()).hasSize(1);
            assertThat(board.getBoardDetail()).isNotNull();
            assertThat(board.getProductInfoNotice()).isNotNull();
        }

        @Test
        @DisplayName("title이 null이면 예외가 발생한다")
        void failWhenTitleIsNull() {
            // given & when & then
            assertThatThrownBy(() -> Board.sellerCreate(
                store, null, 10000, "RATE", 10,
                3000, 30000, false, "T_09_10", "NORMAL",
                "CJ대한통운", productInfoNotice, boardDetail, List.of()
            ))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_BOARD_TITLE);
        }

        @Test
        @DisplayName("title이 빈 문자열이면 예외가 발생한다")
        void failWhenTitleIsBlank() {
            // given & when & then
            assertThatThrownBy(() -> Board.sellerCreate(
                store, "  ", 10000, "RATE", 10,
                3000, 30000, false, "T_09_10", "NORMAL",
                "CJ대한통운", productInfoNotice, boardDetail, List.of()
            ))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_BOARD_TITLE);
        }

        @Test
        @DisplayName("price가 음수이면 예외가 발생한다")
        void failWhenPriceIsNegative() {
            // given & when & then
            assertThatThrownBy(() -> Board.sellerCreate(
                store, "테스트 게시글", -1, "RATE", 10,
                3000, 30000, false, "T_09_10", "NORMAL",
                "CJ대한통운", productInfoNotice, boardDetail, List.of()
            ))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_BOARD_PRICE);
        }

        @Test
        @DisplayName("deliveryFee가 음수이면 예외가 발생한다")
        void failWhenDeliveryFeeIsNegative() {
            // given & when & then
            assertThatThrownBy(() -> Board.sellerCreate(
                store, "테스트 게시글", 10000, "RATE", 0,
                -1, 30000, false, "T_09_10", "NORMAL",
                "CJ대한통운", productInfoNotice, boardDetail, List.of()
            ))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_DELIVERY_FEE);
        }
    }

    @Nested
    @DisplayName("할인율(RATE) 타입 할인 검증")
    class DiscountRateValidation {

        @Test
        @DisplayName("RATE 할인율이 100을 초과하면 예외가 발생한다")
        void failWhenRateExceeds100() {
            // given & when & then
            assertThatThrownBy(() -> Board.sellerCreate(
                store, "테스트 게시글", 10000, "RATE", 101,
                3000, 30000, false, "T_09_10", "NORMAL",
                "CJ대한통운", productInfoNotice, boardDetail, List.of()
            ))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_BOARD_DISCOUNT);
        }

        @Test
        @DisplayName("RATE 할인율이 음수이면 예외가 발생한다")
        void failWhenRateIsNegative() {
            // given & when & then
            assertThatThrownBy(() -> Board.sellerCreate(
                store, "테스트 게시글", 10000, "RATE", -1,
                3000, 30000, false, "T_09_10", "NORMAL",
                "CJ대한통운", productInfoNotice, boardDetail, List.of()
            ))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_BOARD_DISCOUNT);
        }
    }

    @Nested
    @DisplayName("할인금액(AMOUNT) 타입 할인 검증")
    class DiscountAmountValidation {

        @Test
        @DisplayName("AMOUNT 할인금액이 가격을 초과하면 예외가 발생한다")
        void failWhenAmountExceedsPrice() {
            // given & when & then
            assertThatThrownBy(() -> Board.sellerCreate(
                store, "테스트 게시글", 10000, "AMOUNT", 10001,
                3000, 30000, false, "T_09_10", "NORMAL",
                "CJ대한통운", productInfoNotice, boardDetail, List.of()
            ))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_BOARD_DISCOUNT);
        }

        @Test
        @DisplayName("AMOUNT 할인금액이 음수이면 예외가 발생한다")
        void failWhenAmountIsNegative() {
            // given & when & then
            assertThatThrownBy(() -> Board.sellerCreate(
                store, "테스트 게시글", 10000, "AMOUNT", -1,
                3000, 30000, false, "T_09_10", "NORMAL",
                "CJ대한통운", productInfoNotice, boardDetail, List.of()
            ))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_BOARD_DISCOUNT);
        }
    }

    @Nested
    @DisplayName("할인가격 계산")
    class DiscountPriceCalculation {

        @Test
        @DisplayName("RATE 타입: 10000원에 10% 할인 시 discountPrice는 9000이다")
        void calculateDiscountPriceWithRate() {
            // given & when
            Board board = Board.sellerCreate(
                store, "테스트 게시글", 10000, "RATE", 10,
                3000, 30000, false, "T_09_10", "NORMAL",
                "CJ대한통운", productInfoNotice, boardDetail, List.of()
            );

            // then
            assertThat(board.getDiscountPrice()).isEqualTo(9000);
            assertThat(board.getDiscountRate()).isEqualTo(10);
        }

        @Test
        @DisplayName("AMOUNT 타입: 10000원에 2000원 할인 시 discountPrice는 8000이다")
        void calculateDiscountPriceWithAmount() {
            // given & when
            Board board = Board.sellerCreate(
                store, "테스트 게시글", 10000, "AMOUNT", 2000,
                3000, 30000, false, "T_09_10", "NORMAL",
                "CJ대한통운", productInfoNotice, boardDetail, List.of()
            );

            // then
            assertThat(board.getDiscountPrice()).isEqualTo(8000);
            assertThat(board.getDiscountRate()).isEqualTo(20);
        }

        @Test
        @DisplayName("RATE 타입: 할인율이 0이면 discountPrice는 원래 가격과 동일하다")
        void calculateDiscountPriceWithZeroRate() {
            // given & when
            Board board = Board.sellerCreate(
                store, "테스트 게시글", 10000, "RATE", 0,
                3000, 30000, false, "T_09_10", "NORMAL",
                "CJ대한통운", productInfoNotice, boardDetail, List.of()
            );

            // then
            assertThat(board.getDiscountPrice()).isEqualTo(10000);
            assertThat(board.getDiscountRate()).isZero();
        }

        @Test
        @DisplayName("AMOUNT 타입: 가격이 0이면 discountRate는 0이다")
        void calculateDiscountRateWithZeroPrice() {
            // given & when
            Board board = Board.sellerCreate(
                store, "테스트 게시글", 0, "AMOUNT", 0,
                3000, 30000, false, "T_09_10", "NORMAL",
                "CJ대한통운", productInfoNotice, boardDetail, List.of()
            );

            // then
            assertThat(board.getDiscountPrice()).isZero();
            assertThat(board.getDiscountRate()).isZero();
        }
    }
}
