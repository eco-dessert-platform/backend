package com.bbangle.bbangle.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.BoardImageDetailResponse;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.dto.ProductResponse;
import com.bbangle.bbangle.board.dto.product.ProductOrderResponse;
import com.bbangle.bbangle.board.dto.product.ProductOrderResponseBase;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.ranking.UpdateBoardStatistic;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushType;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

class ProductServiceTest extends AbstractIntegrationTest {

    final Long NOT_EXSIST_ID = -1L;

    @Test
    @DisplayName("유효하지 않은 boardId로 조회 시 BbangleException을 발생시킨다")
    void throwNotBoard() {
        assertThrows(BbangleException.class,
            () -> productService.getProductResponse(NOT_EXSIST_ID));

        assertThrows(BbangleException.class,
            () -> productService.getProductResponseWithPush(NOT_EXSIST_ID, NOT_EXSIST_ID));
    }



    @Nested
    @DisplayName("getTopBoardIds 메서드는")
    class FindProductDtoById {

        private Member testMember;
        private Board testBoard;
        private Product testProduct;
        private Push testPush;

        @BeforeEach
        void setUp() {
            // Given: 테스트 데이터를 세팅합니다.
            testMember = memberRepository.save(MemberFixture.createKakaoMember());

            testBoard = fixtureBoard(Collections.emptyMap());

            testProduct = Product.builder()
                .title("Sample Product")
                .price(1000)
                .category(Category.COOKIE) // 실제 Category 설정
                .glutenFreeTag(true)
                .highProteinTag(true)
                .sugarFreeTag(true)
                .veganTag(true)
                .ketogenicTag(true)
                .sugars(10)
                .protein(5)
                .carbohydrates(15)
                .fat(3)
                .weight(200)
                .calories(500)
                .monday(true)
                .tuesday(true)
                .wednesday(true)
                .thursday(true)
                .friday(true)
                .saturday(true)
                .sunday(true)
                .orderStartDate(LocalDateTime.of(2024, 1, 1, 0, 0))
                .orderEndDate(LocalDateTime.of(2024, 1, 7, 23, 59))
                .soldout(false)
                .board(testBoard)
                .build();

            productRepository.save(testProduct);

            testPush = Push.builder()
                .productId(testProduct.getId())
                .memberId(testMember.getId())
                .pushType(PushType.DATE) // 실제 PushType 설정
                .days("Monday,Friday")
                .isActive(true)
                .build();

            pushRepository.save(testPush);
        }

        @Test
        void testFindProductDtoById() {
            // When: 실제 서비스 메서드를 호출합니다.
            ProductResponse response = productService.getProductResponseWithPush(testMember.getId(),
                testBoard.getId());

            // Then: 결과를 검증합니다.
            assertThat(response).isNotNull();
            assertThat(response.getBoardIsBundled()).isNotNull(); // isBundled 값 검증
            assertThat(response.getProducts()).isNotEmpty();

            ProductOrderResponseBase orderResponse = response.getProducts().get(0);
            ProductOrderResponse productOrderResponse = orderResponse.getProductOrderResponse();
            assertThat(productOrderResponse.getTitle()).isEqualTo("Sample Product");
            assertThat(productOrderResponse.getPrice()).isEqualTo(1000);
            assertThat(productOrderResponse.getGlutenFreeTag()).isTrue();
        }
    }
}
