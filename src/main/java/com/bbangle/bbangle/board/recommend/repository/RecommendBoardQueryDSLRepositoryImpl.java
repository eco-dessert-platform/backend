package com.bbangle.bbangle.board.recommend.repository;

import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dao.QBoardResponseDao;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.recommend.domain.MemberSegment;
import com.bbangle.bbangle.board.recommend.domain.QSegmentIntolerance;
import com.bbangle.bbangle.board.repository.basic.BoardFilterCreator;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.store.domain.QStore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecommendBoardQueryDSLRepositoryImpl implements RecommendBoardQueryDSLRepository{

    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;
    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;
    private static final QStore store = QStore.store;
    private static final QSegmentIntolerance segmentIntolerance = QSegmentIntolerance.segmentIntolerance;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BoardResponseDao> getRecommendBoardList(
        FilterRequest filterRequest,
        Long cursorId,
        MemberSegment memberSegment
    ) {
        BooleanBuilder filter = new BoardFilterCreator(filterRequest).create();
        BooleanBuilder cursorInfo = getCursor(cursorId);
        BooleanBuilder exclusionCondition = getExclusionCondition(memberSegment);
        List<Long> boardIds = queryFactory.select(board.id)
            .distinct()
            .from(product)
            .join(board)
            .on(product.board.id.eq(board.id))
            .join(boardStatistic)
            .on(boardStatistic.boardId.eq(board.id))
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

        return queryFactory.select(
                new QBoardResponseDao(
                    board.id,
                    store.id,
                    store.name,
                    board.profile,
                    board.title,
                    board.price,
                    product.category,
                    product.glutenFreeTag,
                    product.highProteinTag,
                    product.sugarFreeTag,
                    product.veganTag,
                    product.ketogenicTag,
                    boardStatistic.boardReviewGrade,
                    boardStatistic.boardReviewCount,
                    product.orderEndDate,
                    product.soldout,
                    board.discountRate
                ))
            .from(product)
            .join(board)
            .on(product.board.id.eq(board.id))
            .join(store)
            .on(board.store.id.eq(store.id))
            .join(boardStatistic)
            .on(boardStatistic.boardId.eq(board.id))
            .where(board.id.in(boardIds))
            .orderBy(boardStatistic.boardWishCount.desc())
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

    private BooleanBuilder getCursor(Long cursorId){

        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if (cursorId == null) {
            return cursorBuilder;
        }
        Long targetWishCount = Optional.ofNullable(queryFactory
                .select(boardStatistic.boardWishCount)
                .from(boardStatistic)
                .where(boardStatistic.boardId.eq(cursorId))
                .fetchOne())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

        cursorBuilder.and(boardStatistic.boardWishCount.lt(targetWishCount))
            .or(boardStatistic.boardWishCount.eq(targetWishCount).and(board.id.loe(cursorId)));

        return cursorBuilder;
    }

}
