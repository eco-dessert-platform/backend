package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.dto.BoardDetailResponseDto;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.page.CustomPage;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

public interface BoardService {

    BoardCustomPage<List<BoardResponseDto>> getBoardList(
        FilterRequest filterRequest,
        SortType sort,
        Long cursorId
    );

    @Transactional(readOnly = true)
    BoardDetailResponseDto getBoardDetailResponse(Long memberId, Long boardId);
}
