package com.bbangle.bbangle.board.facade;

import com.bbangle.bbangle.board.service.BoardDetailService;
import com.bbangle.bbangle.board.service.dto.BoardDetailCommand;
import com.bbangle.bbangle.board.service.dto.BoardDetailInfo;
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
