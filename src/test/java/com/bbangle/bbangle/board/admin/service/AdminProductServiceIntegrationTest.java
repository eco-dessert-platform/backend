package com.bbangle.bbangle.board.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.EditStockFlag;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합 테스트] AdminProductService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminProductServiceIntegrationTest {

    @Autowired
    private AdminProductService adminProductService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Nested
    @DisplayName("editProductStock 메서드")
    class EditProductStock {

        @Test
        @DisplayName("INCREASE: 상품 조회 후 재고를 증가시키면 DB에 반영된다")
        void increase_persistsToDatabase() {
            // given
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.crawlingActiveBoardWithStore(store, "테스트게시글"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "테스트상품", 10));

            // when
            adminProductService.editProductStock(product.getId(), 5, EditStockFlag.INCREASE);
            productRepository.flush();

            // then
            Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getStock()).isEqualTo(15);
            assertThat(updatedProduct.isSoldout()).isFalse();
        }

        @Test
        @DisplayName("DECREASE: 상품 조회 후 재고를 감소시키면 DB에 반영된다")
        void decrease_persistsToDatabase() {
            // given
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.crawlingActiveBoardWithStore(store, "테스트게시글"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "테스트상품", 10));

            // when
            adminProductService.editProductStock(product.getId(), 3, EditStockFlag.DECREASE);
            productRepository.flush();

            // then
            Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getStock()).isEqualTo(7);
            assertThat(updatedProduct.isSoldout()).isFalse();
        }

        @Test
        @DisplayName("DECREASE: 재고가 0이 되면 품절 상태가 DB에 반영된다")
        void decrease_toZero_setsSoldoutInDatabase() {
            // given
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.crawlingActiveBoardWithStore(store, "테스트게시글"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "테스트상품", 5));

            // when
            adminProductService.editProductStock(product.getId(), 5, EditStockFlag.DECREASE);
            productRepository.flush();

            // then
            Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getStock()).isZero();
            assertThat(updatedProduct.isSoldout()).isTrue();
        }

        @Test
        @DisplayName("DECREASE: 재고보다 많은 수량 감소 시 예외가 발생하고 DB는 변경되지 않는다")
        void decrease_exceedsStock_throwsExceptionAndRollback() {
            // given
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.crawlingActiveBoardWithStore(store, "테스트게시글"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "테스트상품", 5));

            // when & then
            assertThatThrownBy(() ->
                adminProductService.editProductStock(product.getId(), 10, EditStockFlag.DECREASE)
            ).isInstanceOf(BbangleException.class);

            Product unchangedProduct = productRepository.findById(product.getId()).orElseThrow();
            assertThat(unchangedProduct.getStock()).isEqualTo(5);
        }

        @Test
        @DisplayName("SOLDOUT: 품절 처리하면 재고가 0이 되고 품절 상태가 DB에 반영된다")
        void soldout_persistsToDatabase() {
            // given
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.crawlingActiveBoardWithStore(store, "테스트게시글"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "테스트상품", 100));

            // when
            adminProductService.editProductStock(product.getId(), 0, EditStockFlag.SOLDOUT);
            productRepository.flush();

            // then
            Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getStock()).isZero();
            assertThat(updatedProduct.isSoldout()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 요청하면 예외가 발생한다")
        void notFoundProduct_throwsException() {
            // given
            Long nonExistentProductId = 99999L;

            // when & then
            assertThatThrownBy(() ->
                adminProductService.editProductStock(nonExistentProductId, 10, EditStockFlag.INCREASE)
            ).isInstanceOf(BbangleException.class);
        }
    }
}
