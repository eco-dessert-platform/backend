package com.bbangle.bbangle.board.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.ProductOrderDto;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushType;
import com.bbangle.bbangle.store.domain.Store;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProductRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Nested
    @DisplayName("getTopBoardIds 메서드는")
    class GetTopBoardIds {

        private Board board;

        @BeforeEach
        void init() {
            Store store = fixtureStore(Map.of());

            List<Product> products = List.of(
                fixtureProduct(Map.of("category", Category.BREAD)),
                fixtureProduct(Map.of("category", Category.SNACK)));

            board = fixtureBoard(Map.of("store", store, "productList", products));
        }

        @Test
        @DisplayName("인기순이 높은 스토어 게시글을 순서대로 가져올 수 있다")
        void getPopularBoard() {
            List<Long> boardIds = List.of(board.getId());
            Map<Long, Set<Category>> products = productRepository.getCategoryInfoByBoardId(
                boardIds);

            Set<Category> actualCategories = products.get(board.getId());
            Category[] expectCatetegories = new Category[]{Category.BREAD, Category.SNACK};

            assertThat(actualCategories).containsExactlyInAnyOrder(expectCatetegories);
        }
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
            List<ProductOrderDto> result = productRepository.findProductDtoById(testMember.getId(),
                testBoard.getId());

            // Then: 결과를 검증합니다.
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            ProductOrderDto dto = result.get(0);
            assertThat(dto.getTitle()).isEqualTo("Sample Product");
            assertThat(dto.getPrice()).isEqualTo(1000);
            assertThat(dto.getGlutenFreeTag()).isTrue();
            assertThat(dto.getPushType()).isEqualTo(PushType.DATE);
            assertThat(dto.getIsActive()).isTrue();
            // 필요한 다른 필드도 검증할 수 있습니다.
        }
    }
}
