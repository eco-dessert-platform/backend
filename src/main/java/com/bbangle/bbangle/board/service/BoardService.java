package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.dto.BoardDetailResponseDto;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.page.CursorInfo;
import com.bbangle.bbangle.page.CustomPage;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface BoardService {

    CustomPage<List<BoardResponseDto>> getBoardList(
        String sort,
        Boolean glutenFreeTag,
        Boolean highProteinTag,
        Boolean sugarFreeTag,
        Boolean veganTag,
        Boolean ketogenicTag,
        String category,
        Integer minPrice,
        Integer maxPrice,
        Boolean orderAvailableToday,
        Long cursorId,
        CursorInfo cursorInfo
    );

    @Transactional(readOnly = true)
    BoardDetailResponseDto getBoardDetailResponse(Long memberId, Long boardId);


    Boolean saveBoardDetailHtml(Long boardId, MultipartFile htmlFile);

}
