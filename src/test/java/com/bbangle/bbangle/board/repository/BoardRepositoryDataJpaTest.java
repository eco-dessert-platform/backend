package com.bbangle.bbangle.board.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[Repository] BoardRepository")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BoardRepositoryDataJpaTest {

    @Autowired
    private BoardRepository sut;

    @Autowired
    private EntityManager em;

    @Autowired
    private StoreRepository storeRepository;

    @Nested
    @DisplayName("findBySaleStatusAndIsDeletedFalse")
    class FindBySaleStatusAndIsDeletedFalseTests {

        private Store store;

        @BeforeEach
        void setUp() {
            store = storeRepository.save(StoreFixture.defaultStore());
            em.flush();
            em.clear();
        }

        @Test
        @DisplayName("다양한 SaleStatus 중 PENDING만 반환한다")
        void given_multipleSaleStatuses_when_findBySaleStatusPending_then_returnOnlyPendingBoards() {
            // given
            Board pendingBoard = sut.save(BoardFixture.pendingBoardWithStore(store, "pending1"));
            sut.save(BoardFixture.outOfStockBoardWithStore(store, "outOfStock1"));
            sut.save(BoardFixture.stoppedBoardWithStore(store, "stopped1"));
            sut.save(BoardFixture.bannedBoardWithStore(store, "banned1"));

            em.flush();
            em.clear();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Board> result = sut.findBySaleStatusAndIsDeletedFalse(SaleStatus.PENDING, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(pendingBoard.getId());
            assertThat(result.getTotalElements()).isEqualTo(1L);
        }

        @Test
        @DisplayName("삭제된 상품은 제외하고 활성 상품만 반환한다")
        void given_deletedAndActivePendingBoards_when_findBySaleStatusPending_then_excludeDeletedBoards() {
            // given - Active PENDING 2개 생성
            Board activePending1 = sut.save(BoardFixture.pendingBoardWithStore(store, "active1"));
            Board activePending2 = sut.save(BoardFixture.pendingBoardWithStore(store, "active2"));

            // Deleted PENDING 2개 생성
            Board deletedPending1 = BoardFixture.pendingBoardWithStore(store, "deleted1");
            ReflectionTestUtils.setField(deletedPending1, "isDeleted", true);
            sut.save(deletedPending1);

            Board deletedPending2 = BoardFixture.pendingBoardWithStore(store, "deleted2");
            ReflectionTestUtils.setField(deletedPending2, "isDeleted", true);
            sut.save(deletedPending2);

            em.flush();
            em.clear();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Board> result = sut.findBySaleStatusAndIsDeletedFalse(SaleStatus.PENDING, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent()).extracting(Board::getId)
                .containsExactlyInAnyOrder(activePending1.getId(), activePending2.getId());
            assertThat(result.getTotalElements()).isEqualTo(2L);
        }

        @Test
        @DisplayName("페이지네이션이 올바르게 적용된다")
        void given_moreThanPageSize_when_findBySaleStatusWithPageable_then_applyPagination() {
            // given - 15개의 PENDING 상품 생성
            List<Board> boards = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                boards.add(sut.save(BoardFixture.pendingBoardWithStore(store, "pending" + i)));
            }

            em.flush();
            em.clear();

            // when - page=0, size=10
            Page<Board> firstPage = sut.findBySaleStatusAndIsDeletedFalse(
                SaleStatus.PENDING,
                PageRequest.of(0, 10)
            );

            // then
            assertThat(firstPage.getContent()).hasSize(10);
            assertThat(firstPage.getTotalElements()).isEqualTo(15L);
            assertThat(firstPage.getTotalPages()).isEqualTo(2);
            assertThat(firstPage.hasNext()).isTrue();
            assertThat(firstPage.hasPrevious()).isFalse();

            // when - page=1, size=10
            Page<Board> secondPage = sut.findBySaleStatusAndIsDeletedFalse(
                SaleStatus.PENDING,
                PageRequest.of(1, 10)
            );

            // then
            assertThat(secondPage.getContent()).hasSize(5);
            assertThat(secondPage.getTotalElements()).isEqualTo(15L);
            assertThat(secondPage.hasNext()).isFalse();
            assertThat(secondPage.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("해당 상태의 상품이 없을 때 빈 Page를 반환한다")
        void given_noPendingBoards_when_findBySaleStatusPending_then_returnEmptyPage() {
            // given
            sut.save(BoardFixture.outOfStockBoardWithStore(store, "outOfStock1"));
            sut.save(BoardFixture.stoppedBoardWithStore(store, "stopped1"));
            sut.save(BoardFixture.bannedBoardWithStore(store, "banned1"));

            em.flush();
            em.clear();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Board> result = sut.findBySaleStatusAndIsDeletedFalse(SaleStatus.PENDING, pageable);

            // then
            assertThat(result.isEmpty()).isTrue();
            assertThat(result.getTotalElements()).isEqualTo(0L);
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("createdAt 기준으로 내림차순 정렬된다")
        void given_multipleBoards_when_findBySaleStatusWithPageableSortByCreatedAt_then_orderByCreatedAtDesc() {
            // given - 생성 시간차가 있는 5개 상품
            List<Board> boards = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                boards.add(sut.save(BoardFixture.pendingBoardWithStore(store, "pending" + i)));
                try {
                    Thread.sleep(10);  // 생성 시간 간격
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            em.flush();
            em.clear();

            Pageable pageable = PageRequest.of(0, 10, Sort.by(Order.desc("createdAt")));

            // when
            Page<Board> result = sut.findBySaleStatusAndIsDeletedFalse(SaleStatus.PENDING, pageable);

            // then
            assertThat(result.getContent()).hasSize(5);
            // 역순으로 정렬되어야 함 (가장 최신이 먼저)
            assertThat(result.getContent().get(0).getId()).isEqualTo(boards.get(4).getId());
            assertThat(result.getContent().get(1).getId()).isEqualTo(boards.get(3).getId());
            assertThat(result.getContent().get(2).getId()).isEqualTo(boards.get(2).getId());
            assertThat(result.getContent().get(3).getId()).isEqualTo(boards.get(1).getId());
            assertThat(result.getContent().get(4).getId()).isEqualTo(boards.get(0).getId());
        }
    }
}
