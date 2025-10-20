package com.bbangle.bbangle.board.seller.board.service;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Store;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductInfoNoticeRepository;
import com.bbangle.bbangle.board.repository.StoreRepository;
import com.bbangle.bbangle.board.seller.board.controller.dto.request.BoardUploadRequest_v2;
import com.bbangle.bbangle.board.service.ProductImgService;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardUploadService_v2 {

    private final BoardRepository boardRepository;
    private final StoreRepository storeRepository;
    private final BoardDetailRepository boardDetailRepository;
    private final ProductImgService productImgService;
    private final ProductInfoNoticeRepository productInfoNoticeRepository;

    /**
     * productImg - board 연결도 이 때 진행
     */
    @Transactional
    public long upload(Long storeId, BoardUploadRequest_v2 request) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.STORE_NOT_FOUND));

        Board board = saveBoardWithchildren(store, request);

        productImgService.connectImagesToBoard(
            request.productImgIds(),
            board);

        return board.getId();
    }

    private Board saveBoardWithchildren(Store store, BoardUploadRequest_v2 request) {
        Board board = request.toBoard(store);
        return boardRepository.save(board);
    }
}
