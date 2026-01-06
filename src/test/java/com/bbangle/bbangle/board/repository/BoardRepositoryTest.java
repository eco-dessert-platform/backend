package com.bbangle.bbangle.board.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
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


}