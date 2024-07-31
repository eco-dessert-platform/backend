package com.bbangle.bbangle.board.repository.folder.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import com.querydsl.core.BooleanBuilder;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularBoardQueryProviderTest extends AbstractIntegrationTest {

    Long memberId;
    Long anotherMemberId;
    WishListFolder wishListFolder;
    WishListFolder anotherWishListFolder;
    Long firstSavedId;
    Long lastSavedId;
    Store store;

    @BeforeEach
    void generalSetUp() {
        Member member = MemberFixture.createKakaoMember();
        member = memberRepository.save(member);
        memberId = memberService.getFirstJoinedMember(member);
        Member anotherMember = MemberFixture.createKakaoMember();
        anotherMember = memberRepository.save(anotherMember);
        anotherMemberId = memberService.getFirstJoinedMember(anotherMember);
        store = StoreFixture.storeGenerator();
        store = storeRepository.save(store);

        wishListFolder = wishListFolderRepository.findByMemberId(memberId)
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("기본 폴더가 생성되어 있지 않아 테스트 실패"));
        anotherWishListFolder = wishListFolderRepository.findByMemberId(anotherMemberId)
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("기본 폴더가 생성되어 있지 않아 테스트 실패"));

        for (int i = 0; i < 12; i++) {
            Board createdBoard = BoardFixture.randomBoardWithPrice(store, i * 1000);
            createdBoard = boardRepository.save(createdBoard);

            if (i == 0) {
                firstSavedId = createdBoard.getId();
            }
            if (i == 11) {
                lastSavedId = createdBoard.getId();
            }

            Product product = ProductFixture.randomProduct(createdBoard);
            Product product2 = ProductFixture.randomProduct(createdBoard);
            productRepository.save(product);
            productRepository.save(product2);
            BoardStatistic boardStatistic = BoardStatisticFixture.newBoardStatistic(createdBoard);
            boardStatisticRepository.save(boardStatistic);
            wishListBoardService.wish(memberId, createdBoard.getId(),
                new WishListBoardRequest(wishListFolder.getId()));

            if (i % 3 == 0) {
                wishListBoardService.wish(anotherMemberId, createdBoard.getId(),
                    new WishListBoardRequest(anotherWishListFolder.getId()));
            }
        }
    }

    @Test
    @DisplayName("정상적으로 인기순으로 폴더 안의 게시물을 조회한다.")
    void getBoardResponseDaoWithPopularOrder() {
        //given, when
        List<BoardResponseDao> boardResponseDaoList = new PopularBoardQueryProvider(
            queryFactory,
            new BooleanBuilder(),
            FolderBoardSortType.POPULAR.getOrderSpecifier(),
            wishListFolder)
            .getBoards();

        //then
        assertThat(boardResponseDaoList).hasSize(22);
        List<Long> boardIdListWithHighScore = boardResponseDaoList.stream()
            .limit(8)
            .map(BoardResponseDao::boardId)
            .toList();
        long expectedBoardId = lastSavedId - 2L;
        for (int i = 0; i < 6; i += 2) {
            assertThat(boardIdListWithHighScore.get(i)).isEqualTo(expectedBoardId);
            expectedBoardId -= 3;
        }

        for (long i = lastSavedId; i <= firstSavedId; i++) {
            final long finalizedId = i;
            assertThat(boardResponseDaoList
                .stream()
                .filter(dao -> dao.boardId().equals(finalizedId))
                .count()).isEqualTo(2);
        }
    }

}
