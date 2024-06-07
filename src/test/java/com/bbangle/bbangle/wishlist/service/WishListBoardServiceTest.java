package com.bbangle.bbangle.wishlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.fixture.WishlistFolderFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.ranking.domain.BoardStatistic;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.FolderResponseDto;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WishListBoardServiceTest extends AbstractIntegrationTest {

    private static final String DEFAULT_FOLDER_NAME = "기본 폴더";

    @Autowired
    WishListFolderService wishListFolderService;

    Member member;
    Store store;
    Board board;
    Board board2;

    @BeforeEach
    void setup() {
        member = MemberFixture.createKakaoMember();
        member = memberService.getFirstJoinedMember(member);

        store = StoreFixture.storeGenerator();
        storeRepository.save(store);

        board = BoardFixture.randomBoard(store);
        board2 = BoardFixture.randomBoard(store);
        boardRepository.save(board);
        boardRepository.save(board2);

        BoardStatistic boardStatistic = BoardStatistic.builder()
            .board(board)
            .popularScore(0.0)
            .recommendScore(0.0)
            .build();
        BoardStatistic boardStatistic2 = BoardStatistic.builder()
            .board(board2)
            .popularScore(0.0)
            .recommendScore(0.0)
            .build();

        rankingRepository.save(boardStatistic2);
        rankingRepository.save(boardStatistic);
    }

    @Nested
    @DisplayName("위시리스트 추가 서비스 로직 테스트")
    class WishBoard {

        @Test
        @DisplayName("정상적으로 게시글을 위시리스트에 저장한다")
        void wishBoard() {
            //given
            FolderResponseDto defaultFolder = wishListFolderService.getList(member.getId())
                .stream()
                .filter(folder -> folder.title()
                    .equals(DEFAULT_FOLDER_NAME))
                .findFirst()
                .get();

            //when
            wishListBoardService.wish(member.getId(), board.getId(),
                new WishListBoardRequest(defaultFolder.folderId()));

            //then
            FolderResponseDto afterWishDefaultFolder = wishListFolderService.getList(member.getId())
                .stream()
                .filter(folder -> folder.title()
                    .equals(DEFAULT_FOLDER_NAME))
                .findFirst()
                .get();

            assertThat(afterWishDefaultFolder.productImages()).hasSize(1);
            BoardStatistic boardStatistic = rankingRepository.findByBoardId(board.getId())
                .get();
            assertThat(boardStatistic.getRecommendScore()).isEqualTo(1.0);
            assertThat(boardStatistic.getBasicScore()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("이미 위시리스트에 담긴 게시글은 다른 폴더에 담을 수 없다")
        void cannotWishAlreadyWishedBoard() {
            //given
            WishListFolder wishListFolder = WishlistFolderFixture.createWishlistFolder(member);
            wishListFolderRepository.save(wishListFolder);

            FolderResponseDto defaultFolder = wishListFolderService.getList(member.getId())
                .stream()
                .filter(folder -> folder.title()
                    .equals(DEFAULT_FOLDER_NAME))
                .findFirst()
                .get();

            //when, then
            wishListBoardService.wish(member.getId(), board.getId(),
                new WishListBoardRequest(defaultFolder.folderId()));
            assertThatThrownBy(() -> wishListBoardService.wish(member.getId(), board.getId(),
                new WishListBoardRequest(defaultFolder.folderId())))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.ALREADY_ON_WISHLIST.getMessage());
            assertThatThrownBy(() -> wishListBoardService.wish(member.getId(), board.getId(),
                new WishListBoardRequest(wishListFolder.getId())))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.ALREADY_ON_WISHLIST.getMessage());

        }

    }

    @Nested
    @DisplayName("위시리스트 삭제 서비스 로직 테스트")
    class WishCancelBoard {

        @Test
        @DisplayName("정상적으로 게시글을 위시리스트에 삭제한다")
        void wishBoard() {
            //given
            FolderResponseDto defaultFolder = wishListFolderService.getList(member.getId())
                .stream()
                .filter(folder -> folder.title()
                    .equals(DEFAULT_FOLDER_NAME))
                .findFirst()
                .get();

            //when
            wishListBoardService.wish(member.getId(), board.getId(),
                new WishListBoardRequest(defaultFolder.folderId()));
            wishListBoardService.cancel(member.getId(), board.getId());

            //then
            FolderResponseDto afterWishDefaultFolder = wishListFolderService.getList(member.getId())
                .stream()
                .filter(folder -> folder.title()
                    .equals(DEFAULT_FOLDER_NAME))
                .findFirst()
                .get();

            assertThat(afterWishDefaultFolder.productImages()).hasSize(0);
            BoardStatistic boardStatistic = rankingRepository.findByBoardId(board.getId())
                .get();
            assertThat(boardStatistic.getRecommendScore()).isEqualTo(0.0);
            assertThat(boardStatistic.getBasicScore()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("이미 삭제된 게시글은 다시 삭제할 수 없다")
        void cannotWishAlreadyWishedBoard() {
            //given
            WishListFolder wishListFolder = WishlistFolderFixture.createWishlistFolder(member);
            wishListFolderRepository.save(wishListFolder);

            FolderResponseDto defaultFolder = wishListFolderService.getList(member.getId())
                .stream()
                .filter(folder -> folder.title()
                    .equals(DEFAULT_FOLDER_NAME))
                .findFirst()
                .get();

            //when, then
            wishListBoardService.wish(member.getId(), board.getId(),
                new WishListBoardRequest(defaultFolder.folderId()));
            wishListBoardService.cancel(member.getId(), board.getId());
            assertThatThrownBy(() -> wishListBoardService.cancel(member.getId(), board.getId()))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.WISHLIST_BOARD_NOT_FOUND.getMessage());
        }

    }
}
