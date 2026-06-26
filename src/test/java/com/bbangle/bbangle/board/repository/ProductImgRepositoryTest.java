package com.bbangle.bbangle.board.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductImgFixture;
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

@DisplayName("[Repository] - ProductImgRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductImgRepositoryTest {

    @Autowired
    private ProductImgRepository sut;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ProductImgRepository productImgRepository;

    @Autowired
    private EntityManager em;

    /**
     * 테스트 전 auto increment 초기화
     */
    @BeforeEach
    void resetTable() {
        em.flush(); // 대기 중인 SQL 먼저 반영
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE product_img").executeUpdate(); // AUTO_INCREMENT = 1로 재설정
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    @DisplayName("Board ID 목록으로 ProductImg가 soft delete 처리된다")
    @Test
    void softDeleteProductImgsByBoardIds_success() {
        // given
        Store store = StoreFixture.defaultStore();
        em.persist(store);

        Board board1 = BoardFixture.defaultBoardWithStore(store, "board1");
        Board board2 = BoardFixture.defaultBoardWithStore(store, "board2");
        Board board3 = BoardFixture.defaultBoardWithStore(store, "board3");
        em.persist(board1);
        em.persist(board2);
        em.persist(board3);

        ProductImg img1 = sut.save(ProductImgFixture.defaultProductImgWithProduct(board1));
        ProductImg img2 = sut.save(ProductImgFixture.defaultProductImgWithProduct(board2));
        ProductImg img3 = sut.save(ProductImgFixture.defaultProductImgWithProduct(board3));

        em.flush();
        em.clear();

        List<Long> deleteTargetBoardIds = List.of(board1.getId(), board2.getId());

        // when
        sut.softDeleteByBoardIds(deleteTargetBoardIds);
        em.flush();
        em.clear();

        // then
        Optional<ProductImg> deletedImg1 = sut.findById(img1.getId());
        Optional<ProductImg> deletedImg2 = sut.findById(img2.getId());
        Optional<ProductImg> notDeletedImg = sut.findById(img3.getId());

        assertThat(deletedImg1).isPresent();
        assertThat(deletedImg1.get().isDeleted()).isTrue();
        assertThat(deletedImg2).isPresent();
        assertThat(deletedImg2.get().isDeleted()).isTrue();
        assertThat(notDeletedImg).isPresent();
        assertThat(notDeletedImg.get().isDeleted()).isFalse();
    }

    @Nested
    @DisplayName("findThumbnailImagesByBoardIds() 테스트")
    class FindThumbnailImagesByBoardIdsTest {

        private Board board1;
        private Board board2;

        @BeforeEach
        void setUp() {

            Store store = storeRepository.save(StoreFixture.defaultStore());

            board1 = boardRepository.save(BoardFixture.defaultBoardWithStore(store, "board1"));
            board2 = boardRepository.save(BoardFixture.defaultBoardWithStore(store, "board2"));

            // board1 이미지 (thumbnail + 일반)
            productImgRepository.save(ProductImgFixture.defaultProductImgThumbnail(board1, "img1"));
            productImgRepository.save(ProductImgFixture.defaultProductImgWithProductAndOrder(board1, "img2", 1));

            // board2 이미지 (thumbnail)
            productImgRepository.save(ProductImgFixture.defaultProductImgThumbnail(board2, "img3"));

            em.flush();
            em.clear();
        }

        @Test
        @DisplayName("Board ID 목록에 해당하는 Thumbnail(imgOrder=0)만 조회된다")
        void success_findThumbnailImagesByBoardIds() {

            // given
            List<Long> boardIds = List.of(board1.getId(), board2.getId());

            // when
            List<ProductImg> result = productImgRepository.findThumbnailImagesByBoardIds(boardIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(ProductImg::getImgOrder).containsOnly(0);
            assertThat(result).extracting(img -> img.getBoard().getId())
                .containsExactlyInAnyOrder(board1.getId(), board2.getId());
        }
    }
}