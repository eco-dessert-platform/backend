package com.bbangle.bbangle.wishlist.customer.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.boardstatistic.customer.service.BoardStatisticService;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.member.domain.MemberFixture;
import com.bbangle.bbangle.fixture.wishlist.domain.WishListFolderFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.wishlist.customer.dto.WishListBoardRequest;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[비즈니스 로직] WishListBoardService")
@ExtendWith(MockitoExtension.class)
class WishListBoardServiceTest {

    @InjectMocks
    private WishListBoardService sut;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private WishListFolderRepository wishListFolderRepository;

    @Mock
    private WishListBoardRepository wishlistBoardRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardStatisticService boardStatisticService;

    @Nested
    @DisplayName("cancel 메서드")
    class Cancel {

        @DisplayName("찜 내역이 있으면 삭제하고 통계를 갱신한다.")
        @Test
        void givenWishedBoard_whenCancel_thenDeletesAndUpdatesStatistic() {
            // given
            Long memberId = 1L;
            Long boardId = 10L;
            Member member = MemberFixture.createWithId(memberId);
            WishListBoard wishedBoard = WishListBoard.builder()
                .boardId(boardId)
                .memberId(memberId)
                .wishlistFolderId(1L)
                .build();

            given(memberRepository.findMemberById(memberId)).willReturn(member);
            given(wishlistBoardRepository.findByBoardIdAndMemberId(boardId, memberId))
                .willReturn(Optional.of(wishedBoard));

            // when
            sut.cancel(memberId, boardId);

            // then
            then(wishlistBoardRepository).should().delete(wishedBoard);
            then(boardStatisticService).should().updateWishCount(boardId);
        }

        @DisplayName("찜 내역이 없으면 WISHLIST_BOARD_NOT_FOUND 예외를 던진다.")
        @Test
        void givenNoWishRow_whenCancel_thenThrowsWishlistBoardNotFound() {
            // given
            Long memberId = 1L;
            Long boardId = 10L;
            Member member = MemberFixture.createWithId(memberId);

            given(memberRepository.findMemberById(memberId)).willReturn(member);
            given(wishlistBoardRepository.findByBoardIdAndMemberId(boardId, memberId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.cancel(memberId, boardId))
                .isInstanceOf(BbangleException.class)
                .hasFieldOrPropertyWithValue("bbangleErrorCode", BbangleErrorCode.WISHLIST_BOARD_NOT_FOUND);

            then(wishlistBoardRepository).should(never()).delete(any());
        }
    }

    @Nested
    @DisplayName("wish 메서드")
    class Wish {

        @DisplayName("이미 찜한 게시글이면 저장을 시도하지 않고 ALREADY_ON_WISHLIST 예외를 던진다.")
        @Test
        void givenAlreadyWishedBoard_whenWish_thenThrowsAlreadyOnWishlist() {
            // given
            Long memberId = 1L;
            Long boardId = 10L;
            WishListBoardRequest wishRequest = new WishListBoardRequest(0L);
            Member member = MemberFixture.createWithId(memberId);
            WishListFolder defaultFolder = WishListFolderFixture.defaultFolder(member);
            Board board = BoardFixture.withId(BoardFixture.defaultBoard(), boardId);

            given(wishListFolderRepository.findByMemberAndFolderName(anyLong(), any()))
                .willReturn(Optional.of(defaultFolder));
            given(boardRepository.findById(boardId))
                .willReturn(Optional.of(board));
            given(wishlistBoardRepository.existsByBoardIdAndMemberId(boardId, memberId))
                .willReturn(true);

            // when & then
            assertThatThrownBy(() -> sut.wish(memberId, boardId, wishRequest))
                .isInstanceOf(BbangleException.class)
                .hasFieldOrPropertyWithValue("bbangleErrorCode", BbangleErrorCode.ALREADY_ON_WISHLIST);

            then(wishlistBoardRepository).should(never()).save(any());
        }
    }

}
