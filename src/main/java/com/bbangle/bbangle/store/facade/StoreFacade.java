package com.bbangle.bbangle.store.facade;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.store.service.StoreService;
import com.bbangle.bbangle.store.service.dto.StoreInfo;
import com.bbangle.bbangle.store.service.dto.StoreInfo.AllBoard;
import com.bbangle.bbangle.store.service.dto.StoreInfo.StoreDetail;
import com.bbangle.bbangle.wishlist.service.WishListBoardService;
import com.bbangle.bbangle.wishlist.service.WishListStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StoreFacade {

        private final StoreService storeService;
        private final WishListBoardService wishListBoardService;
        private final WishListStoreService wishListStoreService;

        public List<StoreInfo.BestBoard> getBestBoards(Long memberId, Long storeId) {
                List<Board> boards = storeService.getBestBoards(storeId);
                Map<Long, Boolean> boardWishedMap = wishListBoardService.getBoardWishedMap(memberId, boards);
                return storeService.convertToBestBoard(boards, boardWishedMap);
        }

        public StoreInfo.Store getStoreInfo(Long memberId, Long boardId) {
                StoreInfo.Store storeInfo = storeService.getStoreInfo(boardId);
                boolean existWishListStore = wishListStoreService.existWishListStore(memberId, storeInfo.getId());
                storeService.combineWishedStore(storeInfo, existWishListStore);
                return storeInfo;
        }

        public StoreDetail getStoreDetail(Long memberId, Long storeId) {
                StoreDetail storeDetail = storeService.getStoreDetail(storeId);
                boolean existWishListStore = wishListStoreService.existWishListStore(memberId, storeDetail.getStoreId());
                storeService.combineWishedStoreDetail(storeDetail, existWishListStore);
                return storeDetail;
        }

        public List<AllBoard> getAllBoard(Long memberId, Long storeId, Long cursorId) {
                List<Board> boards = storeService.getBoardsInStore(storeId, cursorId);
                Map<Long, Boolean> boardWishedMap = wishListBoardService.getBoardWishedMap(memberId, boards);
                return storeService.convertToAllBoard(boards, boardWishedMap);
        }

}
