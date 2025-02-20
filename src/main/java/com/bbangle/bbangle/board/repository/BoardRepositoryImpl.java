package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dao.BoardWithTagDao;
import com.bbangle.bbangle.board.dao.QBoardResponseDao;
import com.bbangle.bbangle.board.dao.QBoardWithTagDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.domain.QProductImg;
import com.bbangle.bbangle.board.domain.QRandomBoard;
import com.bbangle.bbangle.board.dto.QTitleDto;
import com.bbangle.bbangle.board.dto.TitleDto;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.basic.BoardFilterCreator;
import com.bbangle.bbangle.board.repository.basic.cursor.BoardCursorGeneratorMapping;
import com.bbangle.bbangle.board.repository.basic.cursor.PreferenceRecommendCursorGenerator;
import com.bbangle.bbangle.board.repository.basic.query.PreferenceRecommendBoardQueryProviderResolver;
import com.bbangle.bbangle.board.repository.folder.cursor.BoardInFolderCursorGeneratorMapping;
import com.bbangle.bbangle.board.repository.folder.query.BoardInFolderQueryGeneratorMapping;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.repository.basic.query.BoardQueryProviderResolver;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.boardstatistic.domain.QBoardPreferenceStatistic;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.preference.domain.Preference;
import com.bbangle.bbangle.preference.domain.PreferenceType;
import com.bbangle.bbangle.preference.domain.QMemberPreference;
import com.bbangle.bbangle.preference.domain.QPreference;
import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
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

    private static final QMemberPreference memberPreference = QMemberPreference.memberPreference;
    private static final QPreference preference = QPreference.preference;
    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;
    private static final QProductImg productImage = QProductImg.productImg;
    private static final QWishListBoard wishListBoard = QWishListBoard.wishListBoard;
    private static final QBoardStatistic boardStatistic = QBoardStatistic.boardStatistic;
    private static final QBoardPreferenceStatistic preferenceStatistic = QBoardPreferenceStatistic.boardPreferenceStatistic;
    private static final QRandomBoard randomBoard = QRandomBoard.randomBoard;
    private static final QStore store = QStore.store;

    private final BoardQueryProviderResolver boardQueryProviderResolver;
    private final PreferenceRecommendBoardQueryProviderResolver preferenceProviderResolver;
    private final BoardCursorGeneratorMapping boardCursorGeneratorMapping;
    private final BoardInFolderCursorGeneratorMapping boardInFolderCursorGeneratorMapping;
    private final PreferenceRecommendCursorGenerator preferenceRecommendCursorGenerator;

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
        Long memberId,
        FilterRequest filterRequest,
        SortType sort,
        Long cursorId
    ) {
        BooleanBuilder filter = new BoardFilterCreator(filterRequest).create();
        BooleanBuilder cursorInfo = boardCursorGeneratorMapping
            .mappingCursorGenerator(sort)
            .getCursor(cursorId);
        OrderSpecifier<?>[] orderExpression = sort.getOrderExpression();
        if(sort == SortType.RECOMMEND && memberId != null){
            PreferenceType selectedPreference = getMemberPreference(memberId).getPreferenceType();
            if(selectedPreference == null){
                throw new BbangleException(BbangleErrorCode.MEMBER_PREFERENCE_NOT_FOUND);
            }
            cursorInfo = preferenceRecommendCursorGenerator.getCursor(cursorId, selectedPreference);
            orderExpression = List.of(QBoardPreferenceStatistic.boardPreferenceStatistic.preferenceScore.desc()).toArray(new OrderSpecifier[0]);
            return preferenceProviderResolver.findBoards(filter, cursorInfo, orderExpression, memberId, selectedPreference);
        }

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
                    board.discountRate,
                    productImage.url)
            )
            .from(board)
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
            .leftJoin(preferenceStatistic)
            .on(board.id.eq(preferenceStatistic.boardId))
            .where(preferenceStatistic.id.isNull())
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
        if(cursorId == null){
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
            .join(randomBoard)
            .on(randomBoard.randomBoardId.eq(board.id))
            .join(boardStatistic)
            .on(boardStatistic.boardId.eq(board.id))
            .where(board.id.in(boardIds))
            .where(randomBoard.id.goe(randomBoardId).and(randomBoard.setNumber.eq(setNumber)))
            .orderBy(randomBoard.id.asc())
            .fetch();
    }

    private Preference getMemberPreference(Long memberId){
        return queryFactory.select(preference)
            .from(preference)
            .join(memberPreference)
            .on(preference.id.eq(memberPreference.preferenceId))
            .where(memberPreference.memberId.eq(memberId))
            .fetchOne();
    }
}
