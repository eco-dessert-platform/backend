package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.dao.BoardWithTagDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.store.domain.Store;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoardRepositoryImplTest extends AbstractIntegrationTest {

    private Store store;
    private Board board1;
    private Board board2;
    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setup(){
        store = StoreFixture.storeGenerator();
        store = storeRepository.save(store);
        board1 = BoardFixture.randomBoard(store);
        board2 = BoardFixture.randomBoard(store);
        board1 = boardRepository.save(board1);
        board2 = boardRepository.save(board2);

        product1 = ProductFixture.randomProduct(board1);
        product2 = ProductFixture.randomProduct(board1);
        product3 = ProductFixture.randomProduct(board2);

        product1 = productRepository.save(product1);
        product2 = productRepository.save(product2);
        product3 = productRepository.save(product3);
    }

    @Test
    @DisplayName("정상적으로 게시글은 존재하지만 취향 기반의 게시글이 랭킹이 존재하지 않을 경우 정상적으로 생성한다.")
    void makeBoardPreferenceRankingSuccessfully() {
        //given, when
        List<BoardWithTagDao> boardWithTagDaos = boardRepository.checkingNullWithPreferenceRanking();

        //then
        int a = 1;
        int b = 2;
    }

}
