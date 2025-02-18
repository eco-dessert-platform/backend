package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.BoardDetail;
import com.bbangle.bbangle.board.dto.BoardUploadRequest;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardUploadService {

    private final BoardRepository boardRepository;
    private final StoreRepository storeRepository;
    private final BoardDetailRepository boardDetailRepository;
    private final ProductImgService productImgService;

    /**
     * productImg - board 연결도 이 때 진행
     */
    @Transactional
    public void upload(Long storeId, BoardUploadRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.STORE_NOT_FOUND));
        Board board = request.toEntity(store);
        BoardDetail boardDetail = request.getBoardDetail().toEntity(board);

        Board savedBoard = boardRepository.save(board);
        boardDetailRepository.save(boardDetail);

        productImgService.connectImagesToBoard(
                request.getProductImgIds(),
                savedBoard);
    }
}
