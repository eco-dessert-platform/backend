package com.bbangle.bbangle.board.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.store.domain.Store;
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
            Map<Long, Set<Category>> products = productRepository.getCategoryInfoByBoardId(boardIds);

            Set<Category> actualCategories = products.get(board.getId());
            Category[]  expectCatetegories = new Category[] {Category.BREAD, Category.SNACK};

            assertThat(actualCategories).containsExactlyInAnyOrder(expectCatetegories);
        }
    }

}
