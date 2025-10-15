package com.bbangle.bbangle.board.customer.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Store;
import com.bbangle.bbangle.board.repository.StoreRepository;
import com.bbangle.bbangle.board.customer.service.dto.StoreInfo;
import com.bbangle.bbangle.board.customer.service.dto.StoreInfo.StoreDetail;
import com.bbangle.bbangle.board.customer.service.mapper.StoreInfoMapper;
import com.bbangle.bbangle.common.page.CursorPageResponse;
import com.bbangle.bbangle.exception.BbangleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;
import static com.bbangle.bbangle.exception.BbangleErrorCode.BOARD_NOT_FOUND;
import static com.bbangle.bbangle.exception.BbangleErrorCode.STORE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class StoreService {

        private final StoreRepository storeRepository;
        private final StoreInfoMapper storeInfoMapper;

        public StoreInfo.Store getStoreInfo(Long boardId) {
                Store store = storeRepository.findByBoardId(boardId)
                    .orElseThrow(() -> new BbangleException(BOARD_NOT_FOUND));

                return storeInfoMapper.toStoreInfo(store);
        }

        public void combineWishedStore(StoreInfo.Store storeInfo, Boolean isWished) {
                storeInfo.updateWished(isWished);
        }

        public StoreDetail getStoreDetail(Long storeId) {
                Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new BbangleException(STORE_NOT_FOUND));

                return storeInfoMapper.toStoreDetail(store);
        }

        public void combineWishedStoreDetail(StoreInfo.StoreDetail storeDetail, Boolean isWished) {
                storeDetail.updateWished(isWished);
        }

        public List<Board> getBoardsInStore(Long storeId, Long boardIdAsCursorId) {
                return storeRepository.findBoards(storeId, boardIdAsCursorId);
        }

        public List<StoreInfo.AllBoard> convertToAllBoard(List<Board> boards, Map<Long, Boolean> boardWishedMap) {
                return boards.stream().map(board ->
                        storeInfoMapper.toAllBoard(board, boardWishedMap.getOrDefault(board.getId(), false)))
                    .toList();
        }

        public CursorPageResponse<StoreInfo.AllBoard> getCursorByAllBoards(List<StoreInfo.AllBoard> allBoards) {
                return CursorPageResponse.of(allBoards, BOARD_PAGE_SIZE, StoreInfo.AllBoard::boardId);
        }

        public List<Board> getBestBoards(Long storeId) {
                return storeRepository.findBestBoards(storeId);
        }

        public List<StoreInfo.BestBoard> convertToBestBoard(List<Board> boards,
            Map<Long, Boolean> boardWishedMap) {
                return boards.stream().map(board ->
                        storeInfoMapper.toBestBoard(board,
                            boardWishedMap.getOrDefault(board.getId(), false)))
                    .toList();
        }

}
