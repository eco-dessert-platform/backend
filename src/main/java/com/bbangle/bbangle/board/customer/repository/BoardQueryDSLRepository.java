package com.bbangle.bbangle.board.customer.repository;

import com.bbangle.bbangle.board.customer.repository.dao.BoardThumbnailDao;
import com.bbangle.bbangle.board.customer.repository.dao.BoardWithTagDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.customer.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.customer.dto.TitleDto;
import com.querydsl.core.types.OrderSpecifier;
import java.util.List;

public interface BoardQueryDSLRepository {

    List<BoardThumbnailDao> getThumbnailBoardsByIds(List<Long> boardIds, OrderSpecifier<?>[] orderCondition, Long memberId);

    List<TitleDto> findAllTitle();

    List<BoardAndImageDto> findBoardAndBoardImageByBoardId(Long boardId);

    List<Board> checkingNullRanking();

    List<BoardWithTagDao> checkingNullWithPreferenceRanking();

    List<Long> getLikedContentsIds(List<Long> responseList, Long memberId);

    List<BoardThumbnailDao> getRandomboardList(Long cursorId, Long memberId, Integer setNumber);

}

