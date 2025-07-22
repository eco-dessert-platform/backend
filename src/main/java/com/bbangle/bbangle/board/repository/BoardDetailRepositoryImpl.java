package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.dto.QSimilarityBoardDto;
import com.bbangle.bbangle.board.dto.SimilarityBoardDto;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QBoardDetail.boardDetail;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.board.domain.QProductImg.productImg;
import static com.bbangle.bbangle.board.domain.QRecommendationSimilarBoard.recommendationSimilarBoard;
import static com.bbangle.bbangle.board.domain.QStore.store;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic.boardStatistic;
import static com.bbangle.bbangle.wishlist.domain.QWishListBoard.wishListBoard;

@Repository
@RequiredArgsConstructor
public class BoardDetailRepositoryImpl implements BoardDetailQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public String findByBoardId(Long boardId) {
        return queryFactory.select(boardDetail.content)
                .from(board)
                .join(board.boardDetail, boardDetail)
                .where(board.id.eq(boardId))
                .fetchOne();
    }

    @Override
    public List<Long> findSimilarityBoardIdsByNotSoldOut(Long boardId, int limit) {
        return queryFactory.select(board.id)
                .from(recommendationSimilarBoard)
                .join(recommendationSimilarBoard.board, board)
                .join(product).on(board.id.eq(product.board.id))
                .where(recommendationSimilarBoard.queryItem.eq(boardId).and(product.soldout.eq(false)))
                .orderBy(recommendationSimilarBoard.rank.asc())
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
                                        productImg.url,
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
                        .join(board.boardStatistic, boardStatistic)
                        .join(productImg).on(productImg.board.id.eq(board.id))
                        .where(productImg.imgOrder.eq(0))
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
