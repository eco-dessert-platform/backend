package com.bbangle.bbangle.board.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.RecommendationSimilarBoard;
import com.bbangle.bbangle.board.dto.SimilarityBoardResponse;
import com.bbangle.bbangle.board.repository.RecommendationSimilarBoardRepository;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.RecommendationSimilarBoardFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.store.domain.Store;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BoardDetailServiceTest extends AbstractIntegrationTest {

    @Autowired
    RecommendationSimilarBoardRepository similarBoardRepository;
    @Autowired
    BoardDetailService boardDetailService;

    @Test
    @DisplayName("모든 추천 게시판 정보를 조회할 수 있다")
    void fetchAllSimilarBoards() {
        // given
        Store store = storeRepository.save(StoreFixture.storeGenerator());
        Board board1 = boardRepository.save(BoardFixture.randomBoard(store));
        Board board2 = boardRepository.save(BoardFixture.randomBoard(store));
        Board board3 = boardRepository.save(BoardFixture.randomBoard(store));
        productRepository.save(ProductFixture.randomProduct(board1));
        productRepository.save(ProductFixture.randomProduct(board2));
        productRepository.save(ProductFixture.randomProduct(board3));
        boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board1));
        boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board2));
        boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board3));
        List<RecommendationSimilarBoard> similarBoards = similarBoardRepository.saveAll(
            RecommendationSimilarBoardFixture.getRandom(
                board1.getId(),
                List.of(board1.getId(), board2.getId(), board3.getId())
            ));

        List<SimilarityBoardResponse> boardResponses = boardDetailService.getSimilarityBoardResponses(
            null, board1.getId());

        similarBoards.sort(Comparator.comparing(dto -> dto.getRank()));

        assertThat(boardResponses).hasSize(3);
        assertThat(boardResponses.get(0).getBoardId()).isEqualTo(
            similarBoards.get(0).getRecommendationItem());
        assertThat(boardResponses.get(1).getBoardId()).isEqualTo(
            similarBoards.get(1).getRecommendationItem());
        assertThat(boardResponses.get(2).getBoardId()).isEqualTo(
            similarBoards.get(2).getRecommendationItem());
    }

    @Test
    @DisplayName("추천 게시판 중 하나가 부족한 경우에도 조회할 수 있다")
    void fetchWithMissingSimilarBoard() {
        // given
        Store store = storeRepository.save(StoreFixture.storeGenerator());
        Board board1 = boardRepository.save(BoardFixture.randomBoard(store));
        Board board2 = boardRepository.save(BoardFixture.randomBoard(store));
        Board board3 = boardRepository.save(BoardFixture.randomBoard(store));
        productRepository.save(ProductFixture.randomProduct(board1));
        productRepository.save(ProductFixture.randomProduct(board2));
        productRepository.save(ProductFixture.randomProduct(board3));
        boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board1));
        boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board2));
        boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board3));
        RecommendationSimilarBoard similarBoard1 = similarBoardRepository.save(
            RecommendationSimilarBoardFixture.getRandomSingleEntity(
                board1, board2, 1
            ));
        RecommendationSimilarBoard similarBoard2 = similarBoardRepository.save(
            RecommendationSimilarBoardFixture.getRandomSingleEntity(
                board1, board3, 2
            ));

        List<SimilarityBoardResponse> boardResponses = boardDetailService.getSimilarityBoardResponses(
            null, board1.getId());

        assertThat(boardResponses).hasSize(3);
        assertThat(boardResponses.get(0).getBoardId()).isEqualTo(
            similarBoard1.getRecommendationItem());
        assertThat(boardResponses.get(1).getBoardId()).isEqualTo(
            similarBoard2.getRecommendationItem());
    }

    @Test
    @DisplayName("추천 게시판 중 하나가 솔드아웃 상태인 경우에도 조회할 수 있다")
    void fetchWithSoldOutSimilarBoard() {
        // given
        Store store = storeRepository.save(StoreFixture.storeGenerator());
        Board board1 = boardRepository.save(BoardFixture.randomBoard(store));
        Board board2 = boardRepository.save(BoardFixture.randomBoard(store));
        Board board3 = boardRepository.save(BoardFixture.randomBoard(store));
        Board board4 = boardRepository.save(BoardFixture.randomBoard(store));
        productRepository.save(ProductFixture.randomProduct(board1));
        productRepository.save(ProductFixture.randomProduct(board2));
        productRepository.save(ProductFixture.randomSoldOutProduct(board3, true));
        productRepository.save(ProductFixture.randomSoldOutProduct(board4, false));
        boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board1));
        boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board2));
        boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board3));
        boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board4));
        RecommendationSimilarBoard similarBoard1 = similarBoardRepository.save(
            RecommendationSimilarBoardFixture.getRandomSingleEntity(
                board1, board1, 1
            ));
        RecommendationSimilarBoard similarBoard2 = similarBoardRepository.save(
            RecommendationSimilarBoardFixture.getRandomSingleEntity(
                board1, board2, 2
            ));
        similarBoardRepository.save(RecommendationSimilarBoardFixture.getRandomSingleEntity(
            board1, board3, 3
        ));

        List<SimilarityBoardResponse> boardResponses = boardDetailService.getSimilarityBoardResponses(
            null, board1.getId());

        assertThat(boardResponses).hasSize(3);
        assertThat(boardResponses.get(0).getBoardId()).isEqualTo(
            similarBoard1.getRecommendationItem());
        assertThat(boardResponses.get(1).getBoardId()).isEqualTo(
            similarBoard2.getRecommendationItem());
        assertThat(boardResponses.get(2).getBoardId()).isEqualTo(board4.getId());
    }
}
