package com.bbangle.bbangle.store.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.BOARD_NOT_FOUND;
import static com.bbangle.bbangle.exception.BbangleErrorCode.STORE_NOT_FOUND;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.BoardInfoDto;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.common.page.CursorPageResponse;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.dto.StoreDetailStoreDto;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.service.dto.StoreInfo;
import com.bbangle.bbangle.store.service.mapper.StoreMapper;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListStore;
import com.bbangle.bbangle.wishlist.repository.WishListBoardRepository;
import com.bbangle.bbangle.wishlist.repository.WishListStoreRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreService {

    private static final int PAGE_SIZE = 10;
    public static final boolean NON_WISHED = false;
    public static final boolean WISHED = true;

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
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

    private void validateNotNull(Object object, BbangleErrorCode errorCode) {
        if (Objects.isNull(object)) {
            throw new BbangleException(errorCode);
        }
    }

    public StoreDetailStoreDto getStoreResponse(Long memberId, Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new BbangleException(STORE_NOT_FOUND));

        if (Objects.isNull(memberId)) {
            return StoreDetailStoreDto.create(store, WishListStore.createEmptyWishList());
        }

        WishListStore wishListStore = wishListStoreRepository.findWishListStore(memberId, storeId)
            .orElse(WishListStore.createEmptyWishList());

        return StoreDetailStoreDto.create(store, wishListStore);
    }

    public CursorPageResponse<BoardInfoDto> getBoardsInStore(Long memberId,
        Long storeId,
        Long boardIdAsCursorId) {

        List<Board> boards = boardRepository.findBoardsByStore(storeId, boardIdAsCursorId);
        List<BoardInfoDto> boardInfos = boards.stream().map(BoardInfoDto::create).toList();

        if (Objects.nonNull(memberId)) {
            List<Long> boardIds = boards.stream().map(Board::getId).toList();
            updateWishListBoard(memberId, boardIds, boardInfos);
        }

        return CursorPageResponse.of(boardInfos, PAGE_SIZE, BoardInfoDto::getBoardId);
    }

    private void updateWishListBoard(Long memberId, List<Long> boardIds,
        List<BoardInfoDto> boardInfos) {
        Map<Long, Boolean> wishListBoardIds = wishListBoardRepository.findByMemberIdAndBoardIds(
                memberId, boardIds)
            .stream()
            .collect(Collectors.toMap(
                WishListBoard::getBoardId,
                wishListBoard -> WISHED
            ));

        boardInfos.forEach(boardInfoDto -> {
            Boolean isWished = wishListBoardIds.getOrDefault(boardInfoDto.getBoardId(), NON_WISHED);
            boardInfoDto.updateIsWished(isWished);
        });
    }

    public List<Board> getBestBoards(Long storeId) {
        return storeRepository.findBestBoards(storeId);
    }

    public List<StoreInfo.BestBoard> convertToBestBoard(List<Board> boards, Map<Long, Boolean> boardWishedMap) {
        return boards.stream().map(board ->
                storeMapper.toBestBoard(board, boardWishedMap.getOrDefault(board.getId(), false)))
            .toList();
    }
}
