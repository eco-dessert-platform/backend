package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.customer.repository.dao.BoardWithTagDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.boardstatistic.domain.BoardPreferenceStatistic;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardPreferenceStatisticFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.board.domain.Store;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoardRepositoryImplTest extends AbstractIntegrationTest {

    private Board board1;
    private Board board2;
    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setup(){
        Store store = StoreFixture.storeGenerator();
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
        Assertions.assertThat(boardWithTagDaos).hasSize(3);
        Assertions.assertThat(boardWithTagDaos.get(0).boardId()).isEqualTo(board1.getId());
        Assertions.assertThat(boardWithTagDaos.get(0).tagsDao().glutenFreeTag()).isEqualTo(product1.isGlutenFreeTag());
        Assertions.assertThat(boardWithTagDaos.get(0).tagsDao().ketogenicTag()).isEqualTo(product1.isKetogenicTag());
        Assertions.assertThat(boardWithTagDaos.get(0).tagsDao().sugarFreeTag()).isEqualTo(product1.isSugarFreeTag());
        Assertions.assertThat(boardWithTagDaos.get(0).tagsDao().highProteinTag()).isEqualTo(product1.isHighProteinTag());
        Assertions.assertThat(boardWithTagDaos.get(0).tagsDao().veganTag()).isEqualTo(product1.isVeganTag());
        Assertions.assertThat(boardWithTagDaos.get(1).boardId()).isEqualTo(board1.getId());
        Assertions.assertThat(boardWithTagDaos.get(1).tagsDao().glutenFreeTag()).isEqualTo(product2.isGlutenFreeTag());
        Assertions.assertThat(boardWithTagDaos.get(1).tagsDao().ketogenicTag()).isEqualTo(product2.isKetogenicTag());
        Assertions.assertThat(boardWithTagDaos.get(1).tagsDao().sugarFreeTag()).isEqualTo(product2.isSugarFreeTag());
        Assertions.assertThat(boardWithTagDaos.get(1).tagsDao().highProteinTag()).isEqualTo(product2.isHighProteinTag());
        Assertions.assertThat(boardWithTagDaos.get(1).tagsDao().veganTag()).isEqualTo(product2.isVeganTag());
        Assertions.assertThat(boardWithTagDaos.get(2).boardId()).isEqualTo(board2.getId());
        Assertions.assertThat(boardWithTagDaos.get(2).tagsDao().glutenFreeTag()).isEqualTo(product3.isGlutenFreeTag());
        Assertions.assertThat(boardWithTagDaos.get(2).tagsDao().ketogenicTag()).isEqualTo(product3.isKetogenicTag());
        Assertions.assertThat(boardWithTagDaos.get(2).tagsDao().sugarFreeTag()).isEqualTo(product3.isSugarFreeTag());
        Assertions.assertThat(boardWithTagDaos.get(2).tagsDao().highProteinTag()).isEqualTo(product3.isHighProteinTag());
        Assertions.assertThat(boardWithTagDaos.get(2).tagsDao().veganTag()).isEqualTo(product3.isVeganTag());
    }

    @Test
    @DisplayName("정상적으로 게시글은 존재하지만 취향 기반의 게시글이 랭킹이 존재하지 않을 경우 정상적으로 생성한다.")
    void makeBoardPreferenceRankingSuccessfullyExcludingAlreadyExist() {
        //given
        List<BoardPreferenceStatistic> basicPreferenceStatistic = BoardPreferenceStatisticFixture.createBasicPreferenceStatistic(
            board1.getId());
        preferenceStatisticRepository.saveAll(basicPreferenceStatistic);

        // when
        List<BoardWithTagDao> boardWithTagDaos = boardRepository.checkingNullWithPreferenceRanking();

        //then
        Assertions.assertThat(boardWithTagDaos).hasSize(1);
        Assertions.assertThat(boardWithTagDaos.get(0).boardId()).isEqualTo(board2.getId());
        Assertions.assertThat(boardWithTagDaos.get(0).tagsDao().glutenFreeTag()).isEqualTo(product3.isGlutenFreeTag());
        Assertions.assertThat(boardWithTagDaos.get(0).tagsDao().ketogenicTag()).isEqualTo(product3.isKetogenicTag());
        Assertions.assertThat(boardWithTagDaos.get(0).tagsDao().sugarFreeTag()).isEqualTo(product3.isSugarFreeTag());
        Assertions.assertThat(boardWithTagDaos.get(0).tagsDao().highProteinTag()).isEqualTo(product3.isHighProteinTag());
        Assertions.assertThat(boardWithTagDaos.get(0).tagsDao().veganTag()).isEqualTo(product3.isVeganTag());
    }

}
