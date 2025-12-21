package com.bbangle.bbangle.store.repository;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.board.domain.QProductImg.productImg;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic.boardStatistic;
import static com.bbangle.bbangle.store.domain.QStore.store;

import com.bbangle.bbangle.board.customer.dto.AiLearningStoreDto;
import com.bbangle.bbangle.board.customer.dto.QAiLearningStoreDto;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreQueryDSLRepository {

    private static final Integer PAGE_SIZE = 20;
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
    public boolean existsByStoreName(String name) {
        Integer count = queryFactory.selectOne()
            .from(store)
            .where(store.name.eq(name))
            .fetchFirst();
        return count != null;
    }

    @Override
    public Optional<Store> findByStoreName(String storeName) {
        return Optional.ofNullable(queryFactory.selectFrom(store)
            .where(store.name.eq(storeName))
            .fetchOne());
    }


    @Override
    public CursorPagination<StoreInfo> findNextCursorPage(List<Long> storeIds) {

        if (storeIds == null || storeIds.isEmpty()) {
            return CursorPagination.of(Collections.emptyList(), PAGE_SIZE, 0L, SellerStoreInfo.StoreInfo::id);
        }
        ///  추출한 아이디를 통해 조회
        List<Store> stores = queryFactory
            .selectFrom(store)
            .where(store.id.in(storeIds))
            .limit(PAGE_SIZE + 1) // PAGE_SIZE는 상수값 20 고정
            .orderBy(store.createdAt.desc(), store.id.desc())
            .fetch();

        List<SellerStoreInfo.StoreInfo> response = stores.stream()
            .map(StoreInfo::from)
            .toList();


        return CursorPagination.of(response, PAGE_SIZE, null, SellerStoreInfo.StoreInfo::id);
    }


    ///  페이징 처리를 위한 스토어 이름 검색 메서드
    @Override
    public List<Store> getStoreByStoreName(String storeName) {

        return queryFactory.selectFrom(store)
            .where(store.name.contains(storeName)
                .and(store.isDeleted.eq(false))
                .and(store.status.eq(StoreStatus.NONE)))
            .limit(PAGE_SIZE + 1)
            .orderBy(store.createdAt.desc(), store.id.desc())
            .stream().toList();
    }
}
