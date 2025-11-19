package com.bbangle.bbangle.store.repository;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.board.domain.QProductImg.productImg;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic.boardStatistic;
import static com.bbangle.bbangle.exception.BbangleErrorCode.STORE_NOT_FOUND;
import static com.bbangle.bbangle.store.domain.QStore.store;

import com.bbangle.bbangle.board.customer.dto.AiLearningStoreDto;
import com.bbangle.bbangle.board.customer.dto.QAiLearningStoreDto;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.common.page.StoreCustomPage;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreQueryDSLRepository {

    private static final Long PAGE_SIZE = 20L;
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Board> findBoards(Long storeId, Long cursorId) {
        return queryFactory.selectFrom(board)
            .join(board.store, store).fetchJoin()
            .leftJoin(board.boardStatistic, boardStatistic).fetchJoin()
            .where(
                getLoeCursorId(cursorId),
                store.id.eq(storeId))
            .orderBy(board.id.desc())
            .fetch();
    }

    private BooleanExpression getLoeCursorId(Long cursorId) {
        if (Objects.isNull(cursorId)) {
            return null;
        }

        return board.id.loe(cursorId);
    }

    @Override
    public Optional<Store> findByBoardId(Long boardId) {
        return Optional.ofNullable(queryFactory.selectFrom(store)
            .join(store.boards, board)
            .where(board.id.eq(boardId))
            .fetchFirst());
    }

    @Override
    public List<AiLearningStoreDto> findAiLearningData() {
        return queryFactory.select(
                new QAiLearningStoreDto(
                    store.id,
                    board.id,
                    store.introduce
                )
            ).from(board)
            .join(board.store, store)
            .orderBy(store.id.asc())
            .fetch();
    }

    @Override
    public List<Board> findBestBoards(Long storeId) {
        return queryFactory.selectFrom(board)
            .distinct()
            .join(board.store, store).fetchJoin()
            .join(board.boardStatistic, boardStatistic).fetchJoin()
            .leftJoin(board.productImgs, productImg)
            .leftJoin(board.products, product)
            .where(store.id.eq(storeId))
            .orderBy(boardStatistic.basicScore.desc())
            .limit(3)
            .fetch();
    }


    @Override
    public Optional<Store> findByStoreName(String storeName) {
        return Optional.ofNullable(queryFactory.selectFrom(store)
            .where(store.name.eq(storeName))
            .fetchOne());
    }


    @Override
    public StoreCustomPage<List<SellerStoreInfo.StoreInfo>> findNextCursorPage(Long cursorId, String searchName) {
        BooleanBuilder whereCondition = getCursorCondition(cursorId);

        if (searchName != null && !searchName.isBlank()) {
            // contains는 SQL의 like '%searchName%' 과 같습니다.
            whereCondition.and(store.name.contains(searchName));
        }

        whereCondition.and(store.isDeleted.eq(false).and(store.status.eq(StoreStatus.NONE)));

        List<Store> stores = queryFactory
            .selectFrom(store)
            .where(whereCondition)
            .limit(PAGE_SIZE + 1) // PAGE_SIZE는 상수값 20 고정
            .orderBy(store.createdAt.desc(), store.id.desc())
            .fetch();

        List<SellerStoreInfo.StoreInfo> response = stores.stream()
            .map(StoreInfo::from)
            .toList();

        return StoreCustomPage.from(response, PAGE_SIZE);
    }


    private BooleanBuilder getCursorCondition(Long cursorId) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (Objects.isNull(cursorId)) {
            return booleanBuilder;
        }
        Long startId = checkingNotificationExistence(cursorId);

        booleanBuilder.and(store.id.lt(startId));
        return booleanBuilder;
    }

    private Long checkingNotificationExistence(Long cursorId) {
        Long checkingId = queryFactory.select(store.id)
            .from(store)
            .where(store.id.eq(cursorId))
            .fetchOne();

        if (Objects.isNull(checkingId)) {
            throw new BbangleException(STORE_NOT_FOUND);
        }

        return cursorId;
    }
}
