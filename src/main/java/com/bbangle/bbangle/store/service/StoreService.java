package com.bbangle.bbangle.store.service;

import com.bbangle.bbangle.board.dto.BoardInfoDto;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.page.StoreDetailCustomPage;
import com.bbangle.bbangle.store.dto.StoreDetailStoreDto;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.wishlist.repository.WishListStoreRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.bbangle.bbangle.board.validator.BoardValidator.validateNotNull;
import static com.bbangle.bbangle.exception.BbangleErrorCode.BOARD_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class StoreService {

    private static final Long PAGE_SIZE = 10L;

    private final StoreRepository storeRepository;
    private final WishListStoreRepository wishListStoreRepository;
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

    public StoreDetailCustomPage<List<BoardInfoDto>> getBoardsInStore(Long memberId,
        Long storeId,
        Long boardIdAsCursorId) {

        List<BoardInfoDto> boardInfoDtos = Objects.nonNull(boardIdAsCursorId) ?
            boardRepository.findBoardsByStoreWithCursor(storeId, memberId, boardIdAsCursorId) :
            boardRepository.findBoardsByStore(storeId, memberId);

        // 이 부분 통합 작업이 필요해 보임
        boolean hasNext = checkingHasNextStoreDetail(boardInfoDtos);

        if (hasNext) {
            boardInfoDtos.remove(
                boardInfoDtos.get(boardInfoDtos.size() - 1));
        }

        return  StoreDetailCustomPage.from(boardInfoDtos, hasNext);
    }

    private boolean checkingHasNextStoreDetail(
        List<BoardInfoDto> storeDetailResponse) {
        return storeDetailResponse.size() >= PAGE_SIZE + 1;
    }


}