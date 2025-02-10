package com.bbangle.bbangle.store.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.BoardInfoDto;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.page.CursorPageResponse;
import com.bbangle.bbangle.store.dto.StoreDetailStoreDto;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.wishlist.repository.WishListStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.bbangle.bbangle.board.validator.BoardValidator.validateNotNull;
import static com.bbangle.bbangle.exception.BbangleErrorCode.BOARD_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class StoreService {

    private static final int PAGE_SIZE = 10;

    private final StoreRepository storeRepository;
    private final WishListStoreRepository wishListStoreRepository;
    private final WishListBoardRepository wishListBoardRepository;
    private final BoardRepository boardRepository;

    public StoreDto getStoreDtoByBoardId(Long memberId, Long boardId) {
        StoreDto storeDto = storeRepository.findByBoardId(boardId);
        validateNotNull(storeDto, BOARD_NOT_FOUND);

        boolean isWished = Objects.nonNull(memberId)
            && wishListStoreRepository.findWishListStore(memberId, storeDto.getId()).isPresent();

        storeDto.updateWished(isWished);

        return storeDto;
    }

    public StoreDetailStoreDto getStoreResponse(Long memberId, Long storeId) {
        return storeRepository.getStoreResponse(memberId, storeId);
    }

    public CursorPageResponse<BoardInfoDto> getBoardsInStore(Long memberId,
                                                             Long storeId,
                                                             Long boardIdAsCursorId) {

        List<Board> boards = boardRepository.findBoardsByStore(memberId, storeId, boardIdAsCursorId);
        List<Long> boardIds = boards.stream().map(Board::getId).toList();

        Map<Long, Boolean> wishListBoardMap = Optional.ofNullable(memberId)
            .map(id -> boardRepository.findWishListBoards(id, boardIds).stream()
            .collect(Collectors.toMap(board -> board.getBoard().getId(), WishListBoard::getWished)))
            .orElseGet(HashMap::new);


        List<BoardInfoDto> boardInfoDtos = boards.stream().map(board -> new BoardInfoDto(
            board.getId(),
            board.getProfile(),
            board.getTitle(),
            board.getPrice(),
            board.getDiscountRate(),
            board.getBoardStatistic().getBoardReviewGrade(),
            (long)board.getReviewCount(),
            board.isSoldout(),
            board.isNotification(),
            board.getTags(),
            board.isBundled(),
            wishListBoardMap.containsKey(board.getId())
        )).toList();


        return CursorPageResponse.of(boardInfoDtos, PAGE_SIZE, BoardInfoDto::getBoardId);
    }
}