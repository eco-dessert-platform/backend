package com.bbangle.bbangle.board.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
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

@DisplayName("[Repository] - BoardRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BoardRepositoryTest {

    @Autowired
    private BoardRepository sut;

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
        em.createNativeQuery("TRUNCATE TABLE product_board").executeUpdate(); // AUTO_INCREMENT = 1로 재설정
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    @DisplayName("Board ID 목록으로 soft delete 처리된다")
    @Test
    void softDeleteByIds_success() {
        // given
        Store store = StoreFixture.defaultStore();
        em.persist(store);
        Board board1 = BoardFixture.defaultBoardWithStore(store, "board1");
        Board board2 = BoardFixture.defaultBoardWithStore(store, "board2");
        Board board3 = BoardFixture.defaultBoardWithStore(store, "board3");
        em.persist(board1);
        em.persist(board2);
        em.persist(board3);
        em.flush();
        em.clear();

        List<Long> deleteTargetIds = List.of(board1.getId(), board2.getId());

        // when
        sut.softDeleteByIds(deleteTargetIds);
        em.flush();
        em.clear();

        // then
        Optional<Board> deletedBoard1 = sut.findById(board1.getId());
        Optional<Board> deletedBoard2 = sut.findById(board2.getId());
        Optional<Board> notDeletedBoard = sut.findById(board3.getId());

        assertThat(deletedBoard1).isPresent();
        assertThat(deletedBoard2).isPresent();
        assertThat(notDeletedBoard).isPresent();
        assertThat(deletedBoard1.get().isDeleted()).isTrue();
        assertThat(deletedBoard2.get().isDeleted()).isTrue();
        assertThat(notDeletedBoard.get().isDeleted()).isFalse();
    }

    @Nested
    @DisplayName("findByIdAndIsDeletedFalse() 테스트")
    class FindByIdAndIsDeletedFalseTest {

        private Store store;

        @BeforeEach
        void setUp() {
            store = storeRepository.save(StoreFixture.defaultStore());
        }

        @Test
        @DisplayName("삭제되지 않은 게시글이 존재하면 조회에 성공한다.")
        void success_findByIdAndIsDeletedFalse() {

            // given
            Board board = sut.save(BoardFixture.defaultBoardWithStore(store, "글루텐프리 식빵"));

            em.flush();
            em.clear();

            // when
            Optional<Board> result = sut.findByIdAndIsDeletedFalse(board.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(board.getId());
            assertThat(result.get().getTitle()).isEqualTo("글루텐프리 식빵");
        }

        @Test
        @DisplayName("삭제된 게시글은 조회되지 않는다.")
        void empty_when_deleted() {

            // given
            Board board = sut.save(BoardFixture.defaultBoardWithStore(store, "삭제된 게시글"));
            board.delete();
            sut.save(board);

            em.flush();
            em.clear();

            // when
            Optional<Board> result = sut.findByIdAndIsDeletedFalse(board.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 boardId를 조회하면 빈 값을 반환한다.")
        void empty_when_notExists() {

            // given
            long invalidBoardId = 999L;

            // when
            Optional<Board> result = sut.findByIdAndIsDeletedFalse(invalidBoardId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("단일 값 연관관계(productInfoNotice, boardDetail)를 함께 조회한다.")
        void success_fetch_withAssociations() {

            // given
            Board board = BoardFixture.defaultBoardWithStore(store, "연관관계 조회 테스트");
            sut.save(board);

            em.flush();
            em.clear();

            // when
            Board result = sut.findByIdAndIsDeletedFalse(board.getId()).orElseThrow();

            // then
            // productInfoNotice, boardDetail이 세팅되지 않은 게시글이므로 null이지만,
            // EntityGraph로 함께 조회되었으므로 접근 시 LazyInitializationException이 발생하지 않아야 한다.
            assertThat(result.getProductInfoNotice()).isNull();
            assertThat(result.getBoardDetail()).isNull();
        }
    }
}