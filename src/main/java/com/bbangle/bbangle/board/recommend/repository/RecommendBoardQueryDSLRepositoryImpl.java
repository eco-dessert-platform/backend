package com.bbangle.bbangle.board.recommend.repository;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.board.recommend.domain.QSegmentIntolerance.segmentIntolerance;
import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic.boardStatistic;

import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.recommend.domain.MemberSegment;
import com.bbangle.bbangle.board.repository.util.BoardFilterCreator;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecommendBoardQueryDSLRepositoryImpl implements RecommendBoardQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> getRecommendBoardList(
            FilterRequest filterRequest, Long cursorId,
            MemberSegment memberSegment
    ) {
        BooleanBuilder filter = new BoardFilterCreator(filterRequest).create();
        BooleanBuilder cursorInfo = getCursor(cursorId);
        BooleanBuilder exclusionCondition = getExclusionCondition(memberSegment);
        return queryFactory.select(board.id)
                .distinct()
                .from(product)
                .join(board)
                .on(product.board.id.eq(board.id))
                .join(board.boardStatistic, boardStatistic)
                .join(segmentIntolerance)
                .on(product.id.eq(segmentIntolerance.productId))
                .where(
                        cursorInfo.and(filter)
                                .and(exclusionCondition)
                                .and(segmentIntolerance.segment.eq(memberSegment.getSegment()))
                )
                .orderBy(boardStatistic.boardWishCount.desc())
                .limit(BOARD_PAGE_SIZE + 1)
                .fetch();
    }

    private BooleanBuilder getExclusionCondition(MemberSegment memberSegment) {
        BooleanBuilder exclusionCondition = new BooleanBuilder();
        if (Boolean.TRUE.equals(memberSegment.getLactoseIntolerance())) {
            exclusionCondition.and(segmentIntolerance.lactoseTag.eq(false));
        }
        if (Boolean.TRUE.equals(memberSegment.getPeanutIntolerance())) {
            exclusionCondition.and(segmentIntolerance.peanutTag.eq(false));
        }
        if (Boolean.TRUE.equals(memberSegment.getPeachIntolerance())) {
            exclusionCondition.and(segmentIntolerance.peachTag.eq(false));
        }
        if (Boolean.TRUE.equals(memberSegment.getRiceIntolerance())) {
            exclusionCondition.and(segmentIntolerance.riceTag.eq(false));
        }
        if (Boolean.TRUE.equals(memberSegment.getTomatoIntolerance())) {
            exclusionCondition.and(segmentIntolerance.tomatoTag.eq(false));
        }
        if (Boolean.TRUE.equals(memberSegment.getPineNutsIntolerance())) {
            exclusionCondition.and(segmentIntolerance.pineNutsTag.eq(false));
        }
        if (Boolean.TRUE.equals(memberSegment.getSoyMilkIntolerance())) {
            exclusionCondition.and(segmentIntolerance.soyMilkTag.eq(false));
        }
        if (Boolean.TRUE.equals(memberSegment.getBeanIntolerance())) {
            exclusionCondition.and(segmentIntolerance.beanTag.eq(false));
        }
        if (Boolean.TRUE.equals(memberSegment.getWalnutsIntolerance())) {
            exclusionCondition.and(segmentIntolerance.walnutsTag.eq(false));
        }

        return exclusionCondition;
    }

    private BooleanBuilder getCursor(Long cursorId) {

        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if (cursorId == null) {
            return cursorBuilder;
        }
        Long targetWishCount = Optional.ofNullable(queryFactory
                        .select(boardStatistic.boardWishCount)
                        .from(boardStatistic)
                        .join(boardStatistic.board, board)
                        .where(board.id.eq(cursorId))
                        .fetchOne())
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

        cursorBuilder.and(boardStatistic.boardWishCount.lt(targetWishCount))
                .or(boardStatistic.boardWishCount.eq(targetWishCount).and(board.id.loe(cursorId)));

        return cursorBuilder;
    }

}
