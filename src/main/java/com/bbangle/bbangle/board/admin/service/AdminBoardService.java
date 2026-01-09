package com.bbangle.bbangle.board.admin.service;

import com.bbangle.bbangle.board.admin.controller.dto.AdminProductResponse;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardDetailRepository;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductImgRepository;
import com.bbangle.bbangle.board.repository.ProductInfoNoticeRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.boardstatistic.repository.BoardStatisticRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminBoardService {

    private final BoardRepository boardRepository;
    private final ProductRepository productRepository;
    private final ProductImgRepository productImgRepository;
    private final BoardDetailRepository boardDetailRepository;
    private final BoardStatisticRepository boardStatisticRepository;
    private final ProductInfoNoticeRepository productInfoNoticeRepository;

    public Page<AdminProductResponse> getAdminBoards(Pageable pageable) {
        Page<Board> boards = boardRepository.findByIsCrawlingTrueAndIsDeletedFalse(pageable);
        return boards.map(AdminProductResponse::fromEntity);
    }

    @Transactional
    public void deleteBoards(List<Long> boardIds) {
        boardRepository.softDeleteByIds(boardIds);
        productRepository.softDeleteByBoardIds(boardIds);
        productImgRepository.softDeleteByBoardIds(boardIds);
        boardDetailRepository.softDeleteByBoardIds(boardIds);
        boardStatisticRepository.softDeleteByBoardIds(boardIds);
        productInfoNoticeRepository.softDeleteByBoardIds(boardIds);
    }

}
