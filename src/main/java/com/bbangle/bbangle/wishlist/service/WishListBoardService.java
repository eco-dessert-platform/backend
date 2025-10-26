package com.bbangle.bbangle.wishlist.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.boardstatistic.customer.service.BoardStatisticService;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.wishlist.repository.WishListFolderRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishListBoardService {

    private static final String DEFAULT_FOLDER_NAME = "기본 폴더";

    private final MemberRepository memberRepository;
    private final WishListFolderRepository wishListFolderRepository;
    private final WishListBoardRepository wishlistBoardRepository;
    private final BoardRepository boardRepository;
    private final BoardStatisticService boardStatisticService;

    @Transactional
    public void wish(Long memberId, Long boardId, WishListBoardRequest wishRequest) {
        WishListFolder wishlistFolder = getWishlistFolder(wishRequest, memberId);

        Board board = boardRepository.findById(boardId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

        makeNewWish(board, wishlistFolder, memberId);

        boardStatisticService.updateWishCount(boardId);
    }


    @Transactional
    public void cancel(Long memberId, Long boardId) {
        Member member = memberRepository.findMemberById(memberId);

        WishListBoard wishedBoard = wishlistBoardRepository.findByBoardIdAndMemberId(boardId,
                member.getId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.WISHLIST_BOARD_NOT_FOUND));

        wishlistBoardRepository.delete(wishedBoard);
        boardStatisticService.updateWishCount(boardId);
    }

    @Transactional
    public void deletedByDeletedMember(Long memberId) {
        Optional<List<WishListBoard>> wishlistProducts = wishlistBoardRepository.findByMemberId(
            memberId);

        wishlistProducts.ifPresent(wishlistBoardRepository::deleteAll);
    }

    private WishListFolder getWishlistFolder(
        WishListBoardRequest wishRequest,
        Long memberId
    ) {
        if (wishRequest.folderId()
            .equals(0L)) {
            return wishListFolderRepository.findByMemberAndFolderName(memberId, DEFAULT_FOLDER_NAME)
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.FOLDER_NOT_FOUND));
        }

        return wishListFolderRepository.findByMemberAndId(memberId, wishRequest.folderId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.FOLDER_NOT_FOUND));
    }

    private void makeNewWish(
        Board board,
        WishListFolder wishlistFolder,
        Long memberId
    ) {
        WishListBoard wishlistBoard = WishListBoard.builder()
            .wishlistFolderId(wishlistFolder.getId())
            .boardId(board.getId())
            .memberId(memberId)
            .build();
        try {
            wishlistBoardRepository.save(wishlistBoard);
        } catch (DataIntegrityViolationException e) {
            throw new BbangleException(BbangleErrorCode.ALREADY_ON_WISHLIST);
        }
    }

    public Map<Long, Boolean> getBoardWishedMap(Long memberId, List<Board> boards) {
        if (Objects.isNull(memberId)) {
            return Collections.emptyMap();
        }

        List<Long> boardIds = boards.stream().map(Board::getId).toList();
        List<Long> likedBoardIds = boardRepository.getLikedContentsIds(boardIds, memberId);
        return boardIds.stream().collect(Collectors.toMap(id -> id, likedBoardIds::contains));
    }

}
