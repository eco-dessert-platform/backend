package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.dto.TitleDto;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.store.dto.BoardsInStoreDto;
import com.bbangle.bbangle.store.dto.PopularBoardDto;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import java.util.List;

public interface BoardQueryDSLRepository {

    List<TitleDto> findAllTitle();

    List<BoardResponseDao> getBoardResponseList(
        FilterRequest filterRequest,
        SortType sort,
        Long cursorId
    );

    List<BoardResponseDao> getAllByFolder(
        FolderBoardSortType sort,
        Long cursorId,
        WishListFolder folder,
        Long memberId
    );

    List<BoardAndImageDto> findBoardAndBoardImageByBoardId(Long boardId);

    List<Long> getTopBoardIds(Long storeId);

    List<PopularBoardDto> getTopBoardInfo(List<Long> boardIds, Long memberId);

    List<Long> getBoardIds(Long boardIdAsCursorId, Long storeId);

    List<BoardsInStoreDto> findByBoardIds(List<Long> cursorIdToBoardIds,
        Long memberId);

    List<Board> checkingNullRanking();

    List<Long> getLikedContentsIds(List<Long> responseList, Long memberId);

    Long getBoardCount(FilterRequest filterRequest);

}

