package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.dto.BoardDetailResponseDto;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.wishListFolder.domain.WishlistFolder;
import java.util.HashMap;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface BoardQueryDSLRepository {

    BoardCustomPage<List<BoardResponseDto>> getBoardResponseDto(
        FilterRequest filterRequest,
        List<Long> matchedIdx,
        Long cursorId
    );

    Slice<BoardResponseDto> getAllByFolder(
        String sort,
        Pageable pageable,
        Long wishListFolderId,
        WishlistFolder wishlistFolder
    );

    BoardDetailResponseDto getBoardDetailResponse(Long memberId, Long boardId);

    HashMap<Long, String> getAllBoardTitle();

    List<BoardResponseDto> updateLikeStatus(List<Long> matchedIdx, List<BoardResponseDto> content);

}

