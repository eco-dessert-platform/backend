package com.bbangle.bbangle.store.facade;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.store.service.StoreService;
import com.bbangle.bbangle.store.service.dto.StoreInfo;
import com.bbangle.bbangle.wishlist.service.WishListBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StoreFacade {

        private final StoreService storeService;
        private final WishListBoardService wishListBoardService;

        public List<StoreInfo.BestBoard> getBestBoards(Long memberId, Long storeId) {
                List<Board> boards = storeService.getBestBoards(storeId);
                Map<Long, Boolean> boardWishedMap = wishListBoardService.getBoardWishedMap(memberId, boards);
                return storeService.convertToBestBoard(boards, boardWishedMap);
        }
}
