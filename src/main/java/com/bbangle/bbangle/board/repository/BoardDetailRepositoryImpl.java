package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QBoardDetail;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.domain.QRecommendationSimilarBoard;
import com.bbangle.bbangle.board.dto.QSimilarityBoardDto;
import com.bbangle.bbangle.board.dto.SimilarityBoardDto;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardDetailRepositoryImpl implements BoardDetailQueryDSLRepository {

    private final JPAQueryFactory queryFactory;
    private static final QBoardDetail boardDetail = QBoardDetail.boardDetail;
    private static final QStore store = QStore.store;
    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;
    private static final QWishListBoard wishListBoard = QWishListBoard.wishListBoard;
    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;
    private static final QRecommendationSimilarBoard similarBoard = QRecommendationSimilarBoard.recommendationSimilarBoard;

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

    @Override
    public List<Long> findSimilarityBoardIdsByNotSoldOut(Long boardId, int limit) {
        return queryFactory.select(similarBoard.recommendationItem)
            .from(similarBoard)
            .join(product).on(similarBoard.recommendationItem.eq(product.board.id))
            .where(similarBoard.queryItem.eq(boardId).and(product.soldout.eq(false)))
            .orderBy(similarBoard.rank.asc())
            .fetch()
            .stream()
            .distinct()
            .limit(limit)
            .toList();
    }

    @Override
    public List<SimilarityBoardDto> findSimilarityBoardByBoardId(Long memberId,
        List<Long> boardIds) {
        BooleanExpression isWished =
            Objects.isNull(memberId) ? Expressions.asBoolean(false) : wishListBoard.id.isNotNull();

        return buildWishList(
            memberId,
            queryFactory.select(
                    new QSimilarityBoardDto(
                        board.id,
                        store.id,
                        board.profile,
                        store.name,
                        board.title,
                        board.discountRate,
                        board.price,
                        boardStatistic.boardReviewGrade,
                        boardStatistic.boardReviewCount,
                        product.glutenFreeTag,
                        product.highProteinTag,
                        product.sugarFreeTag,
                        product.veganTag,
                        product.ketogenicTag,
                        product.soldout,
                        product.category,
                        isWished
                    )
                ).from(board)
                .join(product).on(board.id.eq(product.board.id))
                .join(store).on(board.store.id.eq(store.id))
                .join(boardStatistic).on(board.id.eq(boardStatistic.boardId))
                .where(board.id.in(boardIds))
        ).fetch();
    }

    private <T> JPAQuery<T> buildWishList(Long memberId, JPAQuery<T> query) {
        if (Objects.isNull(memberId)) {
            return query;
        }

        return query.leftJoin(wishListBoard)
            .on(wishListBoard.boardId.eq(board.id).and(wishListBoard.memberId.eq(memberId)));
    }
}
