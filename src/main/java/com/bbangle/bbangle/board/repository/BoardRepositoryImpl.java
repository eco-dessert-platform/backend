package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProductImg;
import com.bbangle.bbangle.board.dto.BoardAllTitleDto;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.dto.QBoardAllTitleDto;
import com.bbangle.bbangle.board.repository.basic.BoardFilterCreator;
import com.bbangle.bbangle.board.repository.basic.cursor.BoardCursorGeneratorMapping;
import com.bbangle.bbangle.board.repository.folder.cursor.BoardInFolderCursorGeneratorMapping;
import com.bbangle.bbangle.board.repository.folder.query.BoardInFolderQueryGeneratorMapping;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.repository.basic.query.BoardQueryProviderResolver;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.store.dto.BoardsInStoreDto;
import com.bbangle.bbangle.store.dto.PopularBoardDto;
import com.bbangle.bbangle.store.dto.QBoardsInStoreDto;
import com.bbangle.bbangle.store.dto.QPopularBoardDto;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.repository.util.WishListBoardFilter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardQueryDSLRepository {

    public static final int BOARD_PAGE_SIZE = 10;

    private static final QBoard board = QBoard.board;
    private static final QProductImg productImage = QProductImg.productImg;
    private static final QWishListBoard wishListBoard = QWishListBoard.wishListBoard;
    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;

    private final BoardQueryProviderResolver boardQueryProviderResolver;
    private final WishListBoardFilter wishListBoardFilter;
    private final BoardCursorGeneratorMapping boardCursorGeneratorMapping;
    private final BoardInFolderCursorGeneratorMapping boardInFolderCursorGeneratorMapping;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BoardAllTitleDto> findTitleByBoardAll() {
        return queryFactory.select(
                new QBoardAllTitleDto(board.id, board.title))
            .from(board)
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
    public List<Long> getTopBoardIds(Long storeId) {
        return queryFactory.select(boardStatistic.boardId)
            .from(boardStatistic)
            .join(board)
            .on(boardStatistic.boardId.eq(board.id))
            .where(board.store.id.eq(storeId))
            .orderBy(boardStatistic.basicScore.desc())
            .limit(3)
            .fetch();
    }

    @Override
    public List<PopularBoardDto> getTopBoardInfo(List<Long> boardIds, Long memberId) {
        return queryFactory
            .select(
                new QPopularBoardDto(
                    board.id,
                    board.profile,
                    board.title,
                    board.price,
                    wishListBoard.id))
            .from(board)
            .leftJoin(wishListBoard)
            .on(wishListBoardFilter.equalMemberId(memberId)
                .and(wishListBoardFilter.equalBoard(board)))
            .where(board.id.in(boardIds))
            .fetch();
    }

    @Override
    public List<Long> getBoardIds(Long boardIdAsCursorId, Long storeId) {
        BooleanBuilder cursorCondition = getBoardCursorCondition(boardIdAsCursorId);

        return queryFactory
            .select(board.id)
            .from(board)
            .where(
                board.store.id.eq(storeId),
                cursorCondition)
            .limit(BOARD_PAGE_SIZE + 1L)
            .orderBy(board.id.desc())
            .fetch();
    }

    private BooleanBuilder getBoardCursorCondition(Long cursorId) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (Objects.isNull(cursorId)) {
            return booleanBuilder;
        }
        Long boardId = checkingBoardExistence(cursorId);

        booleanBuilder.and(board.id.loe(boardId));
        return booleanBuilder;
    }

    private Long checkingBoardExistence(Long cursorId) {
        Long checkingId = queryFactory.select(board.id)
            .from(board)
            .where(board.id.eq(cursorId))
            .fetchOne();

        if (Objects.isNull(checkingId) || checkingId - 1 <= 0) {
            throw new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND);
        }

        return cursorId - 1;
    }

    @Override
    public List<BoardsInStoreDto> findByBoardIds(List<Long> cursorIdToBoardIds,
        Long memberId) {
        return queryFactory.select(
                new QBoardsInStoreDto(
                    board.id,
                    board.profile,
                    board.title,
                    board.price,
                    wishListBoard.id))
            .from(board)
            .leftJoin(wishListBoard).on(
                wishListBoardFilter.equalMemberId(memberId)
                    .and(wishListBoardFilter.equalBoard(board)))
            .where(board.id.in(cursorIdToBoardIds))
            .orderBy(board.id.desc())
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

}
