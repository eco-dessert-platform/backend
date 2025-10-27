package com.bbangle.bbangle.board.customer.service;

import static java.util.Collections.emptyMap;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Store;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

class BestBoardServiceTest extends AbstractIntegrationTest {

    @Nested
    @DisplayName("getTopBoardInfo 메서드는")
    class GetTopBoardInfo {

        private Store store;
        private Board firstBoard;
        private Board secondBoard;
        private Board thirdBoard;
        private Member member;

        @BeforeEach
        void init() {
            store = storeRepository.save(fixtureStore(emptyMap()));

            firstBoard = boardRepository.save(BoardFixture.randomBoard(store));
            secondBoard = boardRepository.save(BoardFixture.randomBoard(store));
            thirdBoard = boardRepository.save(BoardFixture.randomBoard(store));

            productRepository.save(ProductFixture.randomProduct(firstBoard));
            productRepository.save(ProductFixture.randomProduct(secondBoard));
            productRepository.save(ProductFixture.randomProduct(thirdBoard));

            boardStatisticRepository.save(
                BoardStatisticFixture.newBoardStatisticWithBasicScore(firstBoard, 103d));
            boardStatisticRepository.save(
                BoardStatisticFixture.newBoardStatisticWithBasicScore(secondBoard, 102d));
            boardStatisticRepository.save(
                BoardStatisticFixture.newBoardStatisticWithBasicScore(thirdBoard, 101d));

            createWishListStore();
            updateBoardStatistic.updateStatistic();
        }

        void createWishListStore() {
            member = memberRepository.save(Member.builder()
                .build());

            wishListBoardRepository.save(WishListBoard.builder()
                .boardId(firstBoard.getId())
                .memberId(member.getId())
                .build());
        }
    }
}
