package com.bbangle.bbangle.board.customer.facade;

import com.bbangle.bbangle.board.customer.service.BoardDetailService;
import com.bbangle.bbangle.board.customer.service.dto.BoardDetailCommand;
import com.bbangle.bbangle.board.customer.service.dto.BoardDetailInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardDetailFacade {

    private final BoardDetailService boardDetailService;

    public BoardDetailInfo.Main getBoardDetail(BoardDetailCommand.Main command) {
        boardDetailService.increaseVisitor(command);
        return boardDetailService.getBoardDetail(command);
    }
}
