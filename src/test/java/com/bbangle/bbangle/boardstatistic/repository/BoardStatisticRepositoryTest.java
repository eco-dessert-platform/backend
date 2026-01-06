package com.bbangle.bbangle.boardstatistic.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.boardstatistic.domain.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.store.domain.Store;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("[Repository] - BoardStatisticRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BoardStatisticRepositoryTest {

    @Autowired
    private BoardStatisticRepository sut;

    @Autowired
    private EntityManager em;

    /**
     * 테스트 전 auto increment 초기화
     */
    @BeforeEach
    void resetTable() {
        em.flush(); // 대기 중인 SQL 먼저 반영
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE board_statistic").executeUpdate(); // AUTO_INCREMENT = 1로 재설정
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    @DisplayName("Board ID 목록으로 BoardStatistic이 soft delete 처리된다")
    @Test
    void softDeleteBoardStatisticsByBoardIds_success() {
        // given
        Store store = StoreFixture.defaultStore();
        em.persist(store);

        Board board1 = BoardFixture.defaultBoardWithStore(store, "board1");
        Board board2 = BoardFixture.defaultBoardWithStore(store, "board2");
        Board board3 = BoardFixture.defaultBoardWithStore(store, "board3");

        em.persist(board1);
        em.persist(board2);
        em.persist(board3);

        BoardStatistic statistic1 = BoardStatisticFixture.defaultBoardStatisticWithBoard(board1);
        BoardStatistic statistic2 = BoardStatisticFixture.defaultBoardStatisticWithBoard(board2);
        BoardStatistic statistic3 = BoardStatisticFixture.defaultBoardStatisticWithBoard(board3);

        em.persist(statistic1);
        em.persist(statistic2);
        em.persist(statistic3);

        em.flush();
        em.clear();

        List<Long> deleteTargetBoardIds = List.of(
            board1.getId(),
            board2.getId()
        );

        // when
        sut.softDeleteByBoardIds(deleteTargetBoardIds);
        em.flush();
        em.clear();

        // then
        Optional<BoardStatistic> deletedStatistic1 = sut.findById(statistic1.getId());
        Optional<BoardStatistic> deletedStatistic2 = sut.findById(statistic2.getId());
        Optional<BoardStatistic> notDeletedStatistic = sut.findById(statistic3.getId());

        assertThat(deletedStatistic1).isPresent();
        assertThat(deletedStatistic1.get().isDeleted()).isTrue();
        assertThat(deletedStatistic2).isPresent();
        assertThat(deletedStatistic2.get().isDeleted()).isTrue();
        assertThat(notDeletedStatistic).isPresent();
        assertThat(notDeletedStatistic.get().isDeleted()).isFalse();
    }


}