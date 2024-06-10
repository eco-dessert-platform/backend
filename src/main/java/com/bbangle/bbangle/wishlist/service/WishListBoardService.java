package com.bbangle.bbangle.wishlist.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishListBoardService {

    private static final String DEFAULT_FOLDER_NAME = "기본 폴더";
    private static final Double WISH_SCORE = 50.0;
    private static final Double WISH_CANCEL_SCORE = -50.0;

    private final MemberRepository memberRepository;
    private final WishListFolderRepository wishListFolderRepository;
    private final WishListBoardRepository wishlistBoardRepository;
    private final BoardRepository boardRepository;
    private final BoardStatisticRepository boardStatisticRepository;
    private final BoardStatisticService boardStatisticService;

    @Transactional
    public void wish(Long memberId, Long boardId, WishListBoardRequest wishRequest) {
        Member member = memberRepository.findMemberById(memberId);

        WishListFolder wishlistFolder = getWishlistFolder(wishRequest, member);

        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

        validateIsWishAvailable(board.getId(), member.getId());

        makeNewWish(board, wishlistFolder, member);
    }


    @Transactional
    public void cancel(Long memberId, Long boardId) {
        Member member = memberRepository.findMemberById(memberId);
        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

        WishListBoard wishedBoard = wishlistBoardRepository.findByBoardIdAndMemberId(boardId, member.getId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.WISHLIST_BOARD_NOT_FOUND));

        wishlistBoardRepository.delete(wishedBoard);
    }

    @Transactional
    public void deletedByDeletedMember(Long memberId) {
        Optional<List<WishListBoard>> wishlistProducts = wishlistBoardRepository.findByMemberId(
            memberId);

        wishlistProducts.ifPresent(wishlistBoardRepository::deleteAll);
    }

    private void validateIsWishAvailable(Long boardId, Long memberId) {
        if(wishlistBoardRepository.existsByBoardIdAndMemberId(boardId, memberId)){
            throw new BbangleException(BbangleErrorCode.ALREADY_ON_WISHLIST);
        }
    }

    private WishListFolder getWishlistFolder(
        WishListBoardRequest wishRequest,
        Member member
    ) {
        if (wishRequest.folderId().equals(0L)) {
            return wishListFolderRepository.findByMemberAndFolderName(member, DEFAULT_FOLDER_NAME)
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.FOLDER_NOT_FOUND));
        }

        return wishListFolderRepository.findByMemberAndId(member, wishRequest.folderId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.FOLDER_NOT_FOUND));
    }

    private void makeNewWish(
        Board board,
        WishListFolder wishlistFolder,
        Member member
    ) {

        WishListBoard wishlistBoard = WishListBoard.builder()
            .wishlistFolderId(wishlistFolder.getId())
            .boardId(board.getId())
            .memberId(member.getId())
            .build();

        wishlistBoardRepository.save(wishlistBoard);
    }

}
