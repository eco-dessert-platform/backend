package com.bbangle.bbangle.board.repository;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.board.domain.QProductImg.productImg;
import static com.bbangle.bbangle.board.domain.QRandomBoard.randomBoard;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardPreferenceStatistic.boardPreferenceStatistic;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic.boardStatistic;
import static com.bbangle.bbangle.store.domain.QStore.store;
import static com.bbangle.bbangle.wishlist.domain.QWishListBoard.wishListBoard;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dao.BoardWithTagDao;
import com.bbangle.bbangle.board.dao.QBoardResponseDao;
import com.bbangle.bbangle.board.dao.QBoardWithTagDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.dto.QTitleDto;
import com.bbangle.bbangle.board.dto.TitleDto;
import com.bbangle.bbangle.board.repository.util.BoardFilterCreator;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardQueryDSLRepository {

    public static final int BOARD_PAGE_SIZE = 10;
    private static final Long NOT_EXISTS_MEMBER_ID = -9999L;


    private final JPAQueryFactory queryFactory;


    @Override
    public List<BoardResponseDao> getThumbnailBoardsByIds(List<Long> boardIds,
                                                          OrderSpecifier<?>[] orderCondition,
                                                          Long memberId) {
        if (memberId == null) {
            memberId = NOT_EXISTS_MEMBER_ID;
        }

        return queryFactory.select(
                        new QBoardResponseDao(
                                board.id,
                                store.id,
                                store.name,
                                productImg.url,
                                board.title,
                                board.price,
                                wishListBoard.memberId.isNotNull(),
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
                .join(board).on(product.board.id.eq(board.id))
                .join(store).on(board.store.id.eq(store.id))
                .join(board.boardStatistic, boardStatistic)
                .innerJoin(productImg).on(board.id.eq(productImg.board.id).and(productImg.imgOrder.eq(0)))
                .leftJoin(wishListBoard).on(board.id.eq(wishListBoard.boardId).and(wishListBoard.memberId.eq(memberId)))
                .where((board.id.in(boardIds)))
                .orderBy(orderCondition)
                .fetch();
    }

    @Override
    public List<TitleDto> findAllTitle() {
        return queryFactory.select(
                        new QTitleDto(
                                board.id,
                                board.title))
                .from(board)
                .orderBy(board.id.desc())
                .fetch();
    }

    @Override
    public List<BoardAndImageDto> findBoardAndBoardImageByBoardId(Long boardId) {
        return queryFactory.select(
                        Projections.constructor(
                                BoardAndImageDto.class,
                                board.id,
                                board.store.id,
                                productImg.url,
                                productImg.imgOrder,
                                board.title,
                                board.price,
                                board.purchaseUrl,
                                board.status,
                                board.deliveryFee,
                                board.freeShippingConditions,
                                board.discountRate
                        )
                )
                .from(board)
                .leftJoin(productImg)
                .on(productImg.board.eq(board))
                .where(board.id.eq(boardId))
                .fetch();
    }

    @Override
    public List<Board> checkingNullRanking() {
        return queryFactory.select(board)
                .from(board)
                .leftJoin(board.boardStatistic, boardStatistic)
                .where(boardStatistic.id.isNull())
                .fetch();
    }

    @Override
    public List<BoardWithTagDao> checkingNullWithPreferenceRanking() {
        return queryFactory.select(new QBoardWithTagDao(
                        board.id,
                        product.glutenFreeTag,
                        product.highProteinTag,
                        product.sugarFreeTag,
                        product.veganTag,
                        product.ketogenicTag
                ))
                .from(product)
                .join(board)
                .on(product.board.id.eq(board.id))
                .leftJoin(boardPreferenceStatistic)
                .on(board.id.eq(boardPreferenceStatistic.boardId))
                .where(boardPreferenceStatistic.id.isNull())
                .fetch();
    }

    @Override
    public List<Long> getLikedContentsIds(List<Long> responseList, Long memberId) {
        return queryFactory.select(board.id)
                .from(board)
                .leftJoin(wishListBoard)
                .on(board.id.eq(wishListBoard.boardId))
                .where(board.id.in(responseList)
                        .and(wishListBoard.memberId.eq(memberId)))
                .fetch();
    }

    @Override
    public Long getBoardCount(FilterRequest filterRequest) {
        BooleanBuilder filter = new BoardFilterCreator(filterRequest).create();
        return queryFactory.select(board.countDistinct())
                .from(board)
                .leftJoin(product)
                .on(board.id.eq(product.board.id))
                .where(filter)
                .fetchOne();
    }

    @Override
    public List<BoardResponseDao> getRandomboardList(Long cursorId, Long memberId, Integer setNumber) {
        if (cursorId == null) {
            cursorId = 1L;
        }
        Long randomBoardId = queryFactory.select(randomBoard.id)
                .from(randomBoard)
                .where(randomBoard.randomBoardId.eq(cursorId).and(randomBoard.setNumber.eq(setNumber)))
                .fetchOne();
        List<Long> boardIds = queryFactory.select(randomBoard.randomBoardId)
                .from(randomBoard)
                .where(randomBoard.id.goe(randomBoardId).and(randomBoard.setNumber.eq(setNumber)))
                .orderBy(randomBoard.id.asc())
                .limit(BOARD_PAGE_SIZE + 1)
                .fetch();

        return queryFactory.select(
                        new QBoardResponseDao(
                                board.id,
                                store.id,
                                store.name,
                                productImg.url,
                                board.title,
                                board.price,
                                wishListBoard.memberId.isNotNull(),
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
                .join(randomBoard)
                .on(randomBoard.randomBoardId.eq(board.id))
                .join(board.boardStatistic, boardStatistic)
                .innerJoin(productImg).on(board.id.eq(productImg.board.id).and(productImg.imgOrder.eq(0)))
                .leftJoin(wishListBoard).on(board.id.eq(wishListBoard.boardId))
                .where(board.id.in(boardIds))
                .where(randomBoard.id.goe(randomBoardId).and(randomBoard.setNumber.eq(setNumber)))
                .orderBy(randomBoard.id.asc())
                .fetch();
    }

    @Override
    public List<Board> findBoardsByStore(Long storeId, Long boardIdAsCursorId) {
        return queryFactory.selectFrom(board)
                .join(board.store, store).fetchJoin()
                .join(board.boardStatistic, boardStatistic).fetchJoin()
                .where(
                        board.id.loe(boardIdAsCursorId),
                        store.id.eq(storeId))
                .orderBy(board.id.desc())
                .fetch();
    }

}
