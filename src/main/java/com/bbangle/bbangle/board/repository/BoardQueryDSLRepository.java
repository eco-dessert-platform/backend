package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dao.BoardWithTagDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.dto.TitleDto;
import com.querydsl.core.types.OrderSpecifier;
import java.util.List;

public interface BoardQueryDSLRepository {

    List<BoardResponseDao> getThumbnailBoardsByIds(List<Long> boardIds, OrderSpecifier<?>[] orderCondition, Long memberId);

    List<TitleDto> findAllTitle();

    List<BoardAndImageDto> findBoardAndBoardImageByBoardId(Long boardId);

    List<Board> checkingNullRanking();

    List<BoardWithTagDao> checkingNullWithPreferenceRanking();

    List<Long> getLikedContentsIds(List<Long> responseList, Long memberId);

    Long getBoardCount(FilterRequest filterRequest);

    List<BoardResponseDao> getRandomboardList(Long cursorId, Long memberId, Integer setNumber);

    List<Board> findBoardsByStore(Long storeId, Long boardIdAsCursorId);

}

