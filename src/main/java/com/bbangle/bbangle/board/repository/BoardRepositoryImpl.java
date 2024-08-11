package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.domain.QProductImg;
import com.bbangle.bbangle.board.dto.QTitleDto;
import com.bbangle.bbangle.board.dto.TitleDto;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.basic.BoardFilterCreator;
import com.bbangle.bbangle.board.repository.basic.cursor.BoardCursorGeneratorMapping;
import com.bbangle.bbangle.board.repository.folder.cursor.BoardInFolderCursorGeneratorMapping;
import com.bbangle.bbangle.board.repository.folder.query.BoardInFolderQueryGeneratorMapping;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.repository.basic.query.BoardQueryProviderResolver;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.repository.util.WishListBoardFilter;
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

    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;
    private static final QProductImg productImage = QProductImg.productImg;
    private static final QWishListBoard wishListBoard = QWishListBoard.wishListBoard;
    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;

    private final BoardQueryProviderResolver boardQueryProviderResolver;
    private final WishListBoardFilter wishListBoardFilter;
    private final BoardCursorGeneratorMapping boardCursorGeneratorMapping;
    private final BoardInFolderCursorGeneratorMapping boardInFolderCursorGeneratorMapping;

    private final JPAQueryFactory queryFactory;

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
    public List<BoardResponseDao> getBoardResponseList(
        FilterRequest filterRequest,
        SortType sort,
        Long cursorId
    ) {
        BooleanBuilder filter = new BoardFilterCreator(filterRequest).create();
        BooleanBuilder cursorInfo = boardCursorGeneratorMapping
            .mappingCursorGenerator(sort)
            .getCursor(cursorId);
        OrderSpecifier<?>[] orderExpression = sort.getOrderExpression();

        return boardQueryProviderResolver.resolve(sort)
            .findBoards(filter, cursorInfo, orderExpression);
    }

    @Override
    public List<BoardResponseDao> getAllByFolder(
        FolderBoardSortType sort,
        Long cursorId,
        WishListFolder folder,
        Long memberId
    ) {
        BooleanBuilder cursorBuilder = boardInFolderCursorGeneratorMapping
            .mappingCursorGenerator(sort)
            .getCursor(cursorId, folder.getId());
        OrderSpecifier<?> sortBuilder = sort.getOrderSpecifier();

        return BoardInFolderQueryGeneratorMapping.builder()
            .order(sortBuilder)
            .sortType(sort)
            .wishListFolder(folder)
            .jpaQueryFactory(queryFactory)
            .cursorBuilder(cursorBuilder)
            .build()
            .mappingQueryGenerator()
            .getBoards();
    }

    @Override
    public List<BoardAndImageDto> findBoardAndBoardImageByBoardId(Long boardId) {
        return queryFactory.select(
                Projections.constructor(
                    BoardAndImageDto.class,
                    board.id,
                    board.store.id,
                    board.profile,
                    board.title,
                    board.price,
                    board.purchaseUrl,
                    board.status,
                    board.deliveryFee,
                    board.freeShippingConditions,
                    productImage.url)
            ).from(board)
            .leftJoin(productImage)
            .on(productImage.board.eq(board))
            .where(board.id.eq(boardId))
            .fetch();
    }

    @Override
    public List<Board> checkingNullRanking() {
        return queryFactory.select(board)
            .from(board)
            .leftJoin(boardStatistic)
            .on(board.id.eq(boardStatistic.boardId))
            .where(boardStatistic.id.isNull())
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

}
