package com.bbangle.bbangle.board.admin.service;

import com.bbangle.bbangle.board.admin.controller.dto.AdminProductResponse;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
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

    public Page<AdminProductResponse> getAdminBoards(Pageable pageable) {
        Page<Board> boards = boardRepository.findAll(pageable);
        return boards.map(AdminProductResponse::fromEntity);
    }

}
