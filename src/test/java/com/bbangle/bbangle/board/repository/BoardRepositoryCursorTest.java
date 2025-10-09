//package com.bbangle.bbangle.board.repository;
//
//import static org.assertj.core.api.Assertions.*;
//
//import com.bbangle.bbangle.AbstractIntegrationTest;
//
//import com.bbangle.bbangle.board.domain.Board;
//import com.bbangle.bbangle.board.domain.Product;
//import com.bbangle.bbangle.board.dto.BoardResponse;
//import com.bbangle.bbangle.board.dto.FilterRequest;
//import com.bbangle.bbangle.board.constant.SortType;
//import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
//import com.bbangle.bbangle.fixture.BoardFixture;
//import com.bbangle.bbangle.fixture.BoardStatisticFixture;
//import com.bbangle.bbangle.fixture.ProductFixture;
//import com.bbangle.bbangle.fixture.StoreFixture;
//import com.bbangle.bbangle.common.page.CursorPageResponse;
//import com.bbangle.bbangle.board.domain.Store;
//import java.util.ArrayList;
//import java.util.List;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//class BoardRepositoryCursorTest extends AbstractIntegrationTest {
//
//    private static final Long NULL_MEMBER_ID = null;
//
//    @Test
//    @DisplayName("[게시글조회] 추천순 점수 내림차순 정렬 & 커서 정상 확인")
//    void test1() {
//        // given
//        List<Long> idList = new ArrayList<>();
//        Store store  = StoreFixture.storeGenerator();
//        storeRepository.save(store);
//        List<BoardStatistic> boardStatistics = new ArrayList<>();
//        for (int score = 0; score < 5; score++) {
//            Board board = BoardFixture.randomBoard(store);
//            board = boardRepository.save(board);
//            Product product = ProductFixture.randomProduct(board);
//            productRepository.save(product);
//            idList.add(board.getId());
//
//            BoardStatistic boardStatistic = BoardStatisticFixture.newBoardStatisticWithBasicScore(board, (double) score);
//            boardStatistics.add(boardStatistic);
//        }
//        boardStatisticRepository.saveAll(boardStatistics);
//        List<BoardStatistic> all = boardStatisticRepository.findAll();
//
//        FilterRequest filter = FilterRequest.builder()
//            .build();
//        Long cursorId = idList.get(3); // 아마도 3
//
//        // when
//        CursorPageResponse<BoardResponse> resultPage = boardService.getBoards(filter,
//            SortType.RECOMMEND, cursorId, NULL_MEMBER_ID);
//        List<BoardResponse> result = resultPage.getContent();
//
//        // then
//        assertThat(result).hasSize(4);
//        assertThat(result.get(0).getBoardId()).isEqualTo(idList.get(3));
//        assertThat(result.get(1).getBoardId()).isEqualTo(idList.get(2));
//        assertThat(result.get(2).getBoardId()).isEqualTo(idList.get(1));
//        assertThat(result.get(3).getBoardId()).isEqualTo(idList.get(0));
//    }
//
//}
