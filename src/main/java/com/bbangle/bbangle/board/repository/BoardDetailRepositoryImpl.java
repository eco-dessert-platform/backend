package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QBoardDetail;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardDetailRepositoryImpl implements BoardDetailQueryDSLRepository {

    private final JPAQueryFactory queryFactory;
    private static final QBoardDetail boardDetail = QBoardDetail.boardDetail;
    private static final QBoard board = QBoard.board;

    @Override
    public List<String> findByBoardId(Long boardId) {
        return queryFactory.select(
                    boardDetail.url
            ).from(board)
            .join(boardDetail)
            .on(boardDetail.board.eq(board))
            .where(board.id.eq(boardId))
            .orderBy(boardDetail.imgIndex.asc())
            .fetch();
    }
}
