package com.bbangle.bbangle.boardstatistic.service;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.dao.BoardWithTagDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.boardstatistic.domain.BoardPreferenceStatistic;
import com.bbangle.bbangle.boardstatistic.repository.BoardPreferenceStatisticRepository;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.preference.domain.PreferenceType;
import com.bbangle.bbangle.store.domain.Store;
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
    BoardPreferenceStatisticRepository preferenceStatisticRepository;

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

}
