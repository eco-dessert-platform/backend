package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dao.BoardWithTagDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.dto.TitleDto;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import java.util.List;

public interface BoardQueryDSLRepository {

    List<TitleDto> findAllTitle();

    List<BoardResponseDao> getBoardResponseList(
        Long memberId,
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

    List<Board> checkingNullRanking();

    List<BoardWithTagDao> checkingNullWithPreferenceRanking();

    List<Long> getLikedContentsIds(List<Long> responseList, Long memberId);

    Long getBoardCount(FilterRequest filterRequest);

    List<BoardResponseDao> getRandomboardList(Long cursorId, Long memberId, Integer setNumber);

    List<Board> findBoardsByStore(Long storeId, Long boardIdAsCursorId);

}

