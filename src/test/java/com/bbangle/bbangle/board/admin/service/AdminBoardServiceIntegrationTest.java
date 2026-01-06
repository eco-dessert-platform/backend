package com.bbangle.bbangle.board.admin.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.board.admin.controller.dto.AdminProductResponse;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] AdminBoardService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminBoardServiceIntegrationTest {

    @Autowired
    private AdminBoardService adminBoardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("관리자 상품 목록은 크롤링되고 삭제되지 않은 상품만 조회된다")
    void getAdminBoards_filtersByCrawlingAndDeleted() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());

        Board valid1 = BoardFixture.crawlingActiveBoardWithStore(store, "정상상품1");
        Board valid2 = BoardFixture.crawlingActiveBoardWithStore(store, "정상상품2");
        Board deleted = BoardFixture.crawlingDeletedBoardWithStore(store, "삭제상품");
        Board nonCrawling = BoardFixture.defaultBoardWithStore(store, "비크롤링상품");

        boardRepository.saveAll(List.of(valid1, valid2, deleted, nonCrawling));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<AdminProductResponse> result =
            adminBoardService.getAdminBoards(pageable);

        // then
        assertThat(result.getContent())
            .hasSize(2)
            .extracting(AdminProductResponse::productName)
            .containsExactlyInAnyOrder("정상상품1", "정상상품2");

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("페이지 범위를 초과하면 content는 비어 있고 total 정보는 유지된다")
    void getAdminBoards_outOfRangePage_returnsEmptyContent() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());

        boardRepository.save(
            BoardFixture.crawlingActiveBoardWithStore(store, "정상상품")
        );

        Pageable pageable = PageRequest.of(10, 10); // 범위 초과

        // when
        Page<AdminProductResponse> result =
            adminBoardService.getAdminBoards(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("조회 대상이 되는 상품이 하나도 없으면 빈 페이지를 반환한다")
    void getAdminBoards_noValidBoards_returnsEmptyPage() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());

        boardRepository.saveAll(
            List.of(
                BoardFixture.crawlingDeletedBoardWithStore(store, "삭제상품"),
                BoardFixture.nonCrawlingActiveBoardWithStore(store, "비크롤링상품"),
                BoardFixture.nonCrawlingDeletedBoardWithStore(store, "완전제외상품")
            )
        );

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<AdminProductResponse> result =
            adminBoardService.getAdminBoards(pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
    }

    @Test
    @DisplayName("관리자 상품 삭제 시 Board 및 연관 엔티티가 모두 soft delete 된다")
    void deleteBoards_softDeleteAllRelatedEntities() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());
        Board board1 = BoardFixture.crawlingActiveBoardWithStore(store, "상품1");
        Board board2 = BoardFixture.crawlingActiveBoardWithStore(store, "상품2");

        boardRepository.saveAll(List.of(board1, board2));

        List<Long> boardIds = List.of(board1.getId(), board2.getId());

        // when
        adminBoardService.deleteBoards(boardIds);
        boardRepository.flush();

        // then
        List<Board> boards = boardRepository.findAllById(boardIds);
        assertThat(boards)
            .allSatisfy(board -> assertThat(board.isDeleted()).isTrue());
    }

    @Test
    @DisplayName("삭제 대상에 포함되지 않은 상품은 영향을 받지 않는다")
    void deleteBoards_onlyTargetBoardsAreDeleted() {
        // given
        Store store = storeRepository.save(StoreFixture.defaultStore());
        Board target = BoardFixture.crawlingActiveBoardWithStore(store, "삭제대상");
        Board untouched = BoardFixture.crawlingActiveBoardWithStore(store, "유지대상");

        boardRepository.saveAll(List.of(target, untouched));

        // when
        adminBoardService.deleteBoards(List.of(target.getId()));
        boardRepository.flush();

        // then
        Board deletedBoard = boardRepository.findById(target.getId()).orElseThrow();
        Board notDeletedBoard = boardRepository.findById(untouched.getId()).orElseThrow();

        assertThat(deletedBoard.isDeleted()).isTrue();
        assertThat(notDeletedBoard.isDeleted()).isFalse();
    }

}