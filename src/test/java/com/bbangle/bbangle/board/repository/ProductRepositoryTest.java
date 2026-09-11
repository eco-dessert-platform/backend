package com.bbangle.bbangle.board.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("[Repository] - ProductRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository sut;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    /**
     * 테스트 전 auto increment 초기화
     */
    @BeforeEach
    void resetTable() {
        em.flush(); // 대기 중인 SQL 먼저 반영
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE product").executeUpdate(); // AUTO_INCREMENT = 1로 재설정
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    @DisplayName("Board ID 목록으로 연관된 Product가 soft delete 처리된다")
    @Test
    void softDeleteByBoardIds_success() {
        // given
        Store store = StoreFixture.defaultStore();
        em.persist(store);
        Board board1 = BoardFixture.defaultBoardWithStore(store, "board1");
        Board board2 = BoardFixture.defaultBoardWithStore(store, "board2");
        em.persist(board1);
        em.persist(board2);

        Product product1 = ProductFixture.create(board1, "product1");
        Product product2 = ProductFixture.create(board1, "product2");
        Product product3 = ProductFixture.create(board2, "product3");
        em.persist(product1);
        em.persist(product2);
        em.persist(product3);
        em.flush();
        em.clear();

        List<Long> deleteTargetBoardIds = List.of(board1.getId());

        // when
        sut.softDeleteByBoardIds(deleteTargetBoardIds);
        em.flush();
        em.clear();

        // then
        Optional<Product> deletedProduct1 = sut.findById(product1.getId());
        Optional<Product> deletedProduct2 = sut.findById(product2.getId());
        Optional<Product> notDeletedProduct = sut.findById(product3.getId());

        assertThat(deletedProduct1).isPresent();
        assertThat(deletedProduct2).isPresent();
        assertThat(notDeletedProduct).isPresent();

        assertThat(deletedProduct1.get().isDeleted()).isTrue();
        assertThat(deletedProduct2.get().isDeleted()).isTrue();
        assertThat(notDeletedProduct.get().isDeleted()).isFalse();
    }

    @Test
    @DisplayName("삭제된 상품은 조회되지 않는다")
    void success_findByBoardIds_excludeDeletedProduct() {

        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());
        Board board = boardRepository.save(BoardFixture.defaultBoardWithStore(store, "상품"));
        Product deletedProduct = ProductFixture.createWithStock(board, "삭제옵션", 10);
        deletedProduct.delete();

        sut.save(deletedProduct);

        em.flush();
        em.clear();

        // when
        List<Product> result = sut.findByBoardIds(List.of(board.getId()));

        // then
        assertThat(result)
            .extracting(Product::getTitle)
            .doesNotContain("삭제옵션");
    }

    @Nested
    @DisplayName("findAllByBoardIdAndIsDeletedFalse() 테스트")
    class FindAllByBoardIdAndIsDeletedFalseTest {

        private Board board;

        @BeforeEach
        void setUp() {
            Store store = storeRepository.save(StoreFixture.defaultStore());
            board = boardRepository.save(BoardFixture.defaultBoardWithStore(store, "상품 옵션 테스트 게시글"));
        }

        /**
         * 요일 검증(validate)을 통과할 수 있도록 월요일만 true로 설정해 생성한다.
         * Repository 슬라이스 테스트처럼 실제 DB에 저장 가능한 유효한 Product가 필요할 때 사용한다.
         */
        public Product createValidWithBoard(Board board, String title) {
            return new Product(
                board, title, 0, "BREAD", 10,
                false, false, false, false, false,
                true, false, false, false, false, false, false, // monday만 true
                null
            );
        }

        @Test
        @DisplayName("삭제되지 않은 상품 옵션 목록을 조회한다.")
        void success_findAllByBoardIdAndIsDeletedFalse() {

            // given
            sut.save(createValidWithBoard(board, "옵션1"));
            sut.save(createValidWithBoard(board, "옵션2"));

            em.flush();
            em.clear();

            // when
            List<Product> result = sut.findAllByBoardIdAndIsDeletedFalse(board.getId());

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(Product::getTitle)
                .containsExactlyInAnyOrder("옵션1", "옵션2");
        }

        @Test
        @DisplayName("삭제된 상품 옵션은 조회 결과에서 제외된다.")
        void exclude_deletedProduct() {

            // given
            sut.save(createValidWithBoard(board, "활성옵션"));
            Product deletedProduct = sut.save(createValidWithBoard(board, "삭제옵션"));
            deletedProduct.delete();
            sut.save(deletedProduct);

            em.flush();
            em.clear();

            // when
            List<Product> result = sut.findAllByBoardIdAndIsDeletedFalse(board.getId());

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("활성옵션");
        }

        @Test
        @DisplayName("다른 게시글에 속한 상품 옵션은 조회되지 않는다.")
        void exclude_otherBoardProduct() {

            // given
            Store anotherStore = storeRepository.save(StoreFixture.defaultStore("다른스토어"));
            Board anotherBoard = boardRepository.save(BoardFixture.defaultBoardWithStore(anotherStore, "다른 게시글"));

            sut.save(createValidWithBoard(board, "내 게시글 옵션"));
            sut.save(createValidWithBoard(anotherBoard, "다른 게시글 옵션"));

            em.flush();
            em.clear();

            // when
            List<Product> result = sut.findAllByBoardIdAndIsDeletedFalse(board.getId());

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("내 게시글 옵션");
        }

        @Test
        @DisplayName("상품 옵션이 없으면 빈 목록을 반환한다.")
        void empty_when_noProducts() {

            // when
            List<Product> result = sut.findAllByBoardIdAndIsDeletedFalse(board.getId());

            // then
            assertThat(result).isEmpty();
        }
    }
}