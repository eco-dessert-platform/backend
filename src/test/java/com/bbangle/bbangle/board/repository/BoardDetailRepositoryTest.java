package com.bbangle.bbangle.board.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.BoardDetail;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.board.domain.BoardDetailFixture;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
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

@DisplayName("[Repository] - BoardDetailRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BoardDetailRepositoryTest {

    @Autowired
    private BoardDetailRepository sut;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void resetTable() {
        em.flush(); // 대기 중인 SQL 먼저 반영
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE product_detail").executeUpdate(); // AUTO_INCREMENT = 1로 재설정
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    @DisplayName("Board ID 목록으로 BoardDetail이 soft delete 처리된다")
    @Test
    void softDeleteBoardDetailsByBoardIds_success() {
        // given
        Store store = StoreFixture.defaultStore();
        em.persist(store);

        BoardDetail detail1 = BoardDetailFixture.defaultBoardDetailWithBoard("content1");
        BoardDetail detail2 = BoardDetailFixture.defaultBoardDetailWithBoard("content2");
        BoardDetail detail3 = BoardDetailFixture.defaultBoardDetailWithBoard("content3");

        em.persist(detail1);
        em.persist(detail2);
        em.persist(detail3);

        Board board1 = BoardFixture.defaultBoardWithStoreAndDetail(store, detail1, "board1");
        Board board2 = BoardFixture.defaultBoardWithStoreAndDetail(store, detail2, "board2");
        Board board3 = BoardFixture.defaultBoardWithStoreAndDetail(store, detail3, "board3");

        em.persist(board1);
        em.persist(board2);
        em.persist(board3);

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
        Optional<BoardDetail> deletedDetail1 = sut.findById(detail1.getId());
        Optional<BoardDetail> deletedDetail2 = sut.findById(detail2.getId());
        Optional<BoardDetail> notDeletedDetail = sut.findById(detail3.getId());

        assertThat(deletedDetail1).isPresent();
        assertThat(deletedDetail1.get().isDeleted()).isTrue();
        assertThat(deletedDetail2).isPresent();
        assertThat(deletedDetail2.get().isDeleted()).isTrue();
        assertThat(notDeletedDetail).isPresent();
        assertThat(notDeletedDetail.get().isDeleted()).isFalse();
    }

}