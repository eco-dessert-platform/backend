package com.bbangle.bbangle.boardstatistic.customer.service;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.Store;
import com.bbangle.bbangle.board.repository.dao.BoardWithTagDao;
import com.bbangle.bbangle.boardstatistic.domain.BoardPreferenceStatistic;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.preference.domain.PreferenceType;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BoardPreferenceStatisticServiceTest extends AbstractIntegrationTest {

    @Autowired
    BoardPreferenceStatisticService preferenceStatisticService;

    private Board board1;
    private Board board2;
    private Product product1;
    private Product product2;
    private Product product3;
    private List<Board> boardList = new ArrayList<>();

    @Nested
    @DisplayName("게시글에 매칭되는 boardId가 개인 선호 통계 테이블에 없을 때 업데이트")
    class updatingNotGeneratedBoardPreference {

        @BeforeEach
        void setup() {
            Store store = StoreFixture.storeGenerator();
            store = storeRepository.save(store);
            board1 = BoardFixture.randomBoard(store);
            board2 = BoardFixture.randomBoard(store);
            board1 = boardRepository.save(board1);
            board2 = boardRepository.save(board2);
            boardList.add(board1);
            boardList.add(board2);

            product1 = ProductFixture.randomProduct(board1);
            product2 = ProductFixture.randomProduct(board1);
            product3 = ProductFixture.randomProduct(board2);

            product1 = productRepository.save(product1);
            product2 = productRepository.save(product2);
            product3 = productRepository.save(product3);
        }

        @Test
        @DisplayName("정상적으로 존재하지 않는 선호기반 게시글 통계자료를 생성한다")
        void generateBoardPreferenceStatisticList() {
            //given
            preferenceStatisticService.updatingNonRankedBoards();

            //when
            List<BoardWithTagDao> boardWithTagDaos = boardRepository.checkingNullWithPreferenceRanking();
            List<BoardPreferenceStatistic> allPreferenceStatisticList = preferenceStatisticRepository.findAll();

            //then
            Assertions.assertThat(boardWithTagDaos)
                .isEmpty();
            Assertions.assertThat(allPreferenceStatisticList)
                .hasSize(PreferenceType.values().length * boardList.size());
        }

    }

    @Nested
    @DisplayName("기본점수가 다른 선호 유형을 정상적으로 업데이트 한다")
    class UpdatingBasicScoreFollowingBasicScoreInBoardStatistic {

        @BeforeEach
        void setup() {
            Store store = StoreFixture.storeGenerator();
            store = storeRepository.save(store);
            board1 = BoardFixture.randomBoard(store);
            board2 = BoardFixture.randomBoard(store);
            board1 = boardRepository.save(board1);
            board2 = boardRepository.save(board2);
            boardList.add(board1);
            boardList.add(board2);
            BoardStatistic fixture1 = BoardStatisticFixture.newBoardStatisticWithBasicScore(board1,
                3.0);
            BoardStatistic fixture2 = BoardStatisticFixture.newBoardStatisticWithBasicScore(board2,
                24.0);
            boardStatisticRepository.save(fixture1);
            boardStatisticRepository.save(fixture2);

            product1 = ProductFixture.randomProduct(board1);
            product2 = ProductFixture.randomProduct(board1);
            product3 = ProductFixture.randomProduct(board2);

            product1 = productRepository.save(product1);
            product2 = productRepository.save(product2);
            product3 = productRepository.save(product3);
        }

        @Test
        @DisplayName("정상적으로 기본 통계의 추천점수에 맞게 개인추천 통계 테이블에도 업데이트 된다.")
        void updatePreferenceBasicScore() {
            //given
            preferenceStatisticService.updatingNonRankedBoards();
            List<BoardPreferenceStatistic> beforeUpdatingScore = preferenceStatisticRepository.findAll();
            beforeUpdatingScore.forEach(
                preference -> Assertions.assertThat(preference.getBasicScore()).isZero());

            //when
            preferenceStatisticService.checkingBasicScoreAndUpdate();
            List<BoardPreferenceStatistic> afterUpdatingScore = preferenceStatisticRepository.findAll();
            List<BoardStatistic> basicStatisticList = boardStatisticRepository.findAll();

            //then
            for (BoardPreferenceStatistic ps : afterUpdatingScore) {
                for (BoardStatistic bs : basicStatisticList) {
                    if (ps.getBoardId().equals(bs.getBoard().getId())) {
                        Assertions.assertThat(ps.getBasicScore()).isEqualTo(bs.getBasicScore());
                        Assertions.assertThat(ps.getPreferenceScore())
                            .isEqualTo(ps.getBasicScore() * ps.getPreferenceWeight());
                    }
                }
            }
        }
    }

}
