package com.bbangle.bbangle.board.repository.query;

import com.bbangle.bbangle.board.domain.Board;
import com.querydsl.core.BooleanBuilder;
import java.util.List;

// 정렬정보에 따라 쿼리가 아예 다르게 나가야해서 구분하기 위함
public interface BoardQueryProvider {

    List<Board> findBoards(BooleanBuilder filter);
}
