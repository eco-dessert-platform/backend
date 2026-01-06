package com.bbangle.bbangle.board.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.ProductInfoNotice;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductInfoNoticeFixture;
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
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[Repository] - ProductInfoNoticeRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductInfoNoticeRepositoryTest {

    @Autowired
    private ProductInfoNoticeRepository sut;

    @Autowired
    private EntityManager em;

    /**
     * 테스트 전 auto increment 초기화
     */
    @BeforeEach
    void resetTable() {
        em.flush(); // 대기 중인 SQL 먼저 반영
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE product_info_notice").executeUpdate(); // AUTO_INCREMENT = 1로 재설정
        em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }

    @DisplayName("Board ID 목록으로 ProductInfoNotice가 soft delete 처리된다")
    @Test
    void softDeleteProductInfoNoticeByBoardIds_success() {
        // given
        Store store = StoreFixture.defaultStore();
        em.persist(store);

        ProductInfoNotice notice1 = ProductInfoNoticeFixture.defaultNotice();
        ProductInfoNotice notice2 = ProductInfoNoticeFixture.defaultNotice();
        ProductInfoNotice notice3 = ProductInfoNoticeFixture.defaultNotice();

        em.persist(notice1);
        em.persist(notice2);
        em.persist(notice3);

        Board board1 = BoardFixture.defaultBoardWithStore(store, "board1");
        Board board2 = BoardFixture.defaultBoardWithStore(store, "board2");
        Board board3 = BoardFixture.defaultBoardWithStore(store, "board3");

        ReflectionTestUtils.setField(board1, "productInfoNotice", notice1);
        ReflectionTestUtils.setField(board2, "productInfoNotice", notice2);
        ReflectionTestUtils.setField(board3, "productInfoNotice", notice3);

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
        Optional<ProductInfoNotice> deletedNotice1 = sut.findById(notice1.getId());
        Optional<ProductInfoNotice> deletedNotice2 = sut.findById(notice2.getId());
        Optional<ProductInfoNotice> notDeletedNotice = sut.findById(notice3.getId());

        assertThat(deletedNotice1).isPresent();
        assertThat(deletedNotice1.get().isDeleted()).isTrue();
        assertThat(deletedNotice2).isPresent();
        assertThat(deletedNotice2.get().isDeleted()).isTrue();
        assertThat(notDeletedNotice).isPresent();
        assertThat(notDeletedNotice.get().isDeleted()).isFalse();
    }


}