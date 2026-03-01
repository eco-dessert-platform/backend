package com.bbangle.bbangle.board.repository;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.board.domain.QProductImg.productImg;
import static com.bbangle.bbangle.board.domain.QRandomBoard.randomBoard;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardPreferenceStatistic.boardPreferenceStatistic;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic.boardStatistic;
import static com.bbangle.bbangle.store.domain.QStore.store;
import static com.bbangle.bbangle.wishlist.domain.QWishListBoard.wishListBoard;

import com.bbangle.bbangle.board.customer.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.customer.dto.QTitleDto;
import com.bbangle.bbangle.board.customer.dto.TitleDto;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.MainCategory;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.board.domain.SortType;
import com.bbangle.bbangle.board.repository.dao.BoardThumbnailDao;
import com.bbangle.bbangle.board.repository.dao.BoardWithTagDao;
import com.bbangle.bbangle.board.repository.dao.QBoardThumbnailDao;
import com.bbangle.bbangle.board.repository.dao.QBoardWithTagDao;
import com.bbangle.bbangle.board.repository.dao.SellerBoardDao;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardQueryDSLRepository {

    public static final int BOARD_PAGE_SIZE = 10;
    private static final Long NOT_EXISTS_MEMBER_ID = -9999L;


    private final JPAQueryFactory queryFactory;


    @Override
    public List<BoardThumbnailDao> getThumbnailBoardsByIds(List<Long> boardIds,
        OrderSpecifier<?>[] orderCondition,
        Long memberId) {
        if (memberId == null) {
            memberId = NOT_EXISTS_MEMBER_ID;
        }

        return queryFactory.select(
                new QBoardThumbnailDao(
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
            .innerJoin(productImg)
            .on(board.id.eq(productImg.board.id).and(productImg.imgOrder.eq(0)))
            .leftJoin(wishListBoard)
            .on(board.id.eq(wishListBoard.boardId).and(wishListBoard.memberId.eq(memberId)))
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
            .distinct()
            .from(board)
            .leftJoin(wishListBoard)
            .on(board.id.eq(wishListBoard.boardId))
            .where(board.id.in(responseList)
                .and(wishListBoard.memberId.eq(memberId)))
            .fetch();
    }

    @Override
    public List<BoardThumbnailDao> getRandomboardList(Long cursorId, Long memberId,
        Integer setNumber) {
        if (cursorId == null) {
            cursorId = 1L;
        }
        Long randomBoardId = queryFactory.select(randomBoard.id)
            .from(randomBoard)
            .where(randomBoard.randomBoardId.eq(cursorId)
                .and(randomBoard.setNumber.eq(setNumber)))
            .fetchOne();
        List<Long> boardIds = queryFactory.select(randomBoard.randomBoardId)
            .from(randomBoard)
            .where(
                randomBoard.id.goe(randomBoardId).and(randomBoard.setNumber.eq(setNumber)))
            .orderBy(randomBoard.id.asc())
            .limit(BOARD_PAGE_SIZE + 1)
            .fetch();

        return queryFactory.select(
                new QBoardThumbnailDao(
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
            .innerJoin(productImg)
            .on(board.id.eq(productImg.board.id).and(productImg.imgOrder.eq(0)))
            .leftJoin(wishListBoard).on(board.id.eq(wishListBoard.boardId))
            .where(board.id.in(boardIds))
            .where(
                randomBoard.id.goe(randomBoardId).and(randomBoard.setNumber.eq(setNumber)))
            .orderBy(randomBoard.id.asc())
            .fetch();
    }

    @Override
    public Page<SellerBoardDao> findSellerBoards(
        Long storeId,
        SaleStatus saleStatus,
        MainCategory mainCategory,
        Category category,
        String keyword,
        SortType sortBy,
        Pageable pageable
    ) {
        BooleanBuilder where = buildSellerBoardConditions(storeId, saleStatus, mainCategory, category, keyword);

        List<SellerBoardDao> content = queryFactory
            .select(Projections.constructor(SellerBoardDao.class,
                board.id,
                productImg.url,
                board.title,
                board.price,
                board.discountPrice,
                board.discountValue,
                board.deliveryFee,
                board.freeShippingConditions,
                board.saleStatus
            ))
            .from(board)
            .leftJoin(productImg).on(productImg.board.id.eq(board.id).and(productImg.imgOrder.eq(0)))
            .where(where)
            .orderBy(buildOrderSpecifier(sortBy))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(board.count())
            .from(board)
            .where(where)
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    @Override
    public Map<SaleStatus, Long> countSellerBoardsBySaleStatus(Long storeId) {
        List<Tuple> result = queryFactory
            .select(board.saleStatus, board.count())
            .from(board)
            .where(board.store.id.eq(storeId).and(board.isDeleted.isFalse()))
            .groupBy(board.saleStatus)
            .fetch();

        Map<SaleStatus, Long> counts = new EnumMap<>(SaleStatus.class);
        for (SaleStatus status : SaleStatus.values()) {
            counts.put(status, 0L);
        }
        result.forEach(tuple -> counts.put(
            tuple.get(board.saleStatus),
            tuple.get(board.count())
        ));
        return counts;
    }

    private BooleanBuilder buildSellerBoardConditions(
        Long storeId,
        SaleStatus saleStatus,
        MainCategory mainCategory,
        Category category,
        String keyword
    ) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(board.store.id.eq(storeId));
        builder.and(board.isDeleted.isFalse());

        if (saleStatus != null) {
            builder.and(board.saleStatus.eq(saleStatus));
        }
        if (mainCategory != null) {
            List<Category> categories = Category.findByMainCategory(mainCategory);
            builder.and(JPAExpressions.selectOne()
                .from(product)
                .where(product.board.id.eq(board.id)
                    .and(product.category.in(categories))
                    .and(product.isDeleted.isFalse()))
                .exists());
        }
        if (category != null) {
            builder.and(JPAExpressions.selectOne()
                .from(product)
                .where(product.board.id.eq(board.id)
                    .and(product.category.eq(category))
                    .and(product.isDeleted.isFalse()))
                .exists());
        }
        if (keyword != null && !keyword.isBlank()) {
            builder.and(board.title.containsIgnoreCase(keyword));
        }
        return builder;
    }

    private OrderSpecifier<?> buildOrderSpecifier(SortType sortBy) {
        if (sortBy == null) {
            return board.createdAt.desc();
        }
        return switch (sortBy) {
            case OLDEST -> board.createdAt.asc();
            case NAME -> board.title.asc();
            default -> board.createdAt.desc();
        };
    }

}
