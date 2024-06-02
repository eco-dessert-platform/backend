package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.BoardAllTitleDto;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.CursorInfo;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface BoardQueryDSLRepository {

    List<BoardAllTitleDto> findTitleByBoardAll();

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

    List<BoardAndImageDto> findBoardAndBoardImageByBoardId(Long boardId);

    List<Board> checkingNullRanking();

    List<Long> getLikedContentsIds(List<Long> responseList, Long memberId);
}

