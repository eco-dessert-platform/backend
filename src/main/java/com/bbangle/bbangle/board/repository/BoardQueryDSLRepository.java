package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.BoardDetailResponse;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.CursorInfo;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.HashMap;
import java.util.List;

public interface BoardQueryDSLRepository {

    BoardCustomPage<List<BoardResponseDto>> getBoardResponseList(
        FilterRequest filterRequest,
        SortType sort,
        CursorInfo cursorInfo
    );

    Slice<BoardResponseDto> getAllByFolder(
        String sort,
        Pageable pageable,
        Long wishListFolderId,
        WishListFolder wishlistFolder
    );

    BoardDetailResponse getBoardDetailResponse(Long memberId, Long boardId);

    HashMap<Long, String> getAllBoardTitle();

    List<Board> checkingNullRanking();

    List<Long> getLikedContentsIds(List<Long> responseList, Long memberId);

}

