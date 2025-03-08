package com.bbangle.bbangle.board.repository;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.orders.ProductDtoAtBoardDetail;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushType;
import java.util.ArrayList;
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
            board = fixtureBoard(emptyMap());
            boardRepository.save(board);
        }

        @Test
        @DisplayName("인기순이 높은 스토어 게시글을 순서대로 가져올 수 있다")
        void getPopularBoard() {
            Map<Long, Set<Category>> actualProducts = productRepository.getCategoryInfoByBoardId(
                    List.of(board.getId()));

            List<Product> expectedProducts = board.getProducts();
            List categories = new ArrayList<Category>();
            for (Product product : expectedProducts) {
                categories.add(product.getCategory());
            }

            assertThat(actualProducts.get(board.getId())).containsAll(categories);
        }
    }

    @Nested
    @DisplayName("getTopBoardIds 메서드는")
    class FindProductDtoById {

        private Member testMember;
        private Board testBoard;
        private List<Product> testProducts;
        private Push testPush;

        @BeforeEach
        void setUp() {
            // Given: 테스트 데이터를 세팅합니다.
            testMember = memberRepository.save(MemberFixture.createKakaoMember());
            testBoard = boardRepository.save(fixtureBoard(emptyMap()));
            testProducts = testBoard.getProducts();

            testPush = Push.builder()
                    .productId(testProducts.get(0).getId())
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
            List<ProductDtoAtBoardDetail> result = productRepository.findProductDtoById(testMember.getId(),
                    testBoard.getId());

            // Then: 결과를 검증합니다.
            assertThat(result).isNotNull().isNotEmpty();
            assertThat(result).hasSize(testProducts.size());
            assertThat(result).extracting(ProductDtoAtBoardDetail::getTitle)
                    .containsExactlyInAnyOrderElementsOf(testProducts.stream().map(Product::getTitle).toList());
            assertThat(result).extracting(ProductDtoAtBoardDetail::getPrice)
                    .containsExactlyInAnyOrderElementsOf(testProducts.stream().map(Product::getPrice).toList());
            assertThat(result).extracting(ProductDtoAtBoardDetail::getGlutenFreeTag)
                    .containsExactlyInAnyOrderElementsOf(testProducts.stream().map(Product::isGlutenFreeTag).toList());
            assertThat(result).extracting(ProductDtoAtBoardDetail::getPushType)
                    .contains(PushType.DATE);
            assertThat(result).extracting(ProductDtoAtBoardDetail::getIsActive)
                    .contains(true);
            // 필요한 다른 필드도 검증할 수 있습니다.
        }
    }
}
