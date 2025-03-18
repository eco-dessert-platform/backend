package com.bbangle.bbangle.board.recommend.repository;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.recommend.domain.MemberSegment;
import com.querydsl.core.BooleanBuilder;
import java.util.List;

public interface RecommendBoardQueryDSLRepository {

    List<Long> getRecommendBoardList(FilterRequest filter,Long cursorId,
                                                 MemberSegment memberSegment);

}
