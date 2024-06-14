package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.store.dto.QStoreDetailStoreDto;
import com.bbangle.bbangle.store.dto.QStoreDto;
import com.bbangle.bbangle.store.dto.QStoreResponseDto;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.StoreCustomPage;
import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.store.dto.StoreDetailStoreDto;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import com.bbangle.bbangle.wishlist.domain.QWishListStore;
import com.bbangle.bbangle.wishlist.repository.util.WishListStoreFilter;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreQueryDSLRepository {

    private static final Long PAGE_SIZE = 20L;
    private static final Long EMPTY_PAGE_CURSOR = -1L;
    private static final Boolean EMPTY_PAGE_HAS_NEXT = false;

    private final QStore store = QStore.store;
    private final QBoard board = QBoard.board;
    private final QWishListStore wishListStore = QWishListStore.wishListStore;

    private final JPAQueryFactory queryFactory;
    private final WishListStoreFilter wishListStoreFilter;
    private final MemberRepository memberRepository;

    @Override
    public StoreDetailStoreDto getStoreResponse(Long memberId, Long storeId) {
        return queryFactory.select(
                new QStoreDetailStoreDto(
                    store.id,
                    store.profile,
                    store.name,
                    store.introduce,
                    wishListStore.id)
            ).from(store)
            .where(store.id.eq(storeId))
            .leftJoin(wishListStore)
            .on(wishListStoreFilter.equalMemberId(memberId)
                .and(wishListStoreFilter.equalStoreId(store)))
            .fetchOne();
    }

    @Override
    public StoreDto findByBoardId(Long boardId) {
        return queryFactory.select(
                new QStoreDto(
                    store.id,
                    store.name,
                    store.profile
                )
            ).from(board)
            .join(store).on(store.eq(board.store))
            .where(board.id.eq(boardId))
            .fetchFirst();
    }

    @Override
    public HashMap<Long, String> getAllStoreTitle() {
        List<Tuple> fetch = queryFactory
            .select(store.id, store.name)
            .from(store)
            .fetch();

        HashMap<Long, String> storeMap = new HashMap<>();
        fetch.forEach(tuple -> storeMap.put(tuple.get(store.id), tuple.get(store.name)));

        return storeMap;
    }

    @Override
    public StoreCustomPage<List<StoreResponseDto>> getStoreList(Long cursorId, Long memberId) {
        BooleanBuilder cursorCondition = getCursorCondition(cursorId);
        List<StoreResponseDto> responseDtos = queryFactory.select(
                new QStoreResponseDto(
                    store.id,
                    store.name,
                    store.introduce,
                    store.profile
                )
            )
            .from(store)
            .where(cursorCondition)
            .limit(PAGE_SIZE + 1)
            .fetch();
        if (responseDtos.isEmpty()) {
            return StoreCustomPage.from(responseDtos, EMPTY_PAGE_CURSOR, EMPTY_PAGE_HAS_NEXT);
        }

        boolean hasNext = checkingHasNext(responseDtos);
        if (hasNext) {
            responseDtos.remove(responseDtos.get(responseDtos.size() - 1));
        }
        Long nextCursor = responseDtos.get(responseDtos.size() - 1).getStoreId();

        if (Objects.nonNull(memberId)) {
            findNextCursorPageWithLogin(responseDtos, memberId);
        }

        return StoreCustomPage.from(responseDtos, nextCursor, hasNext);
    }

    public List<StoreResponseDto> findNextCursorPageWithLogin(
        List<StoreResponseDto> cursorPage,
        Long memberId
    ) {
        List<Long> pageIds = getContentsIds(cursorPage);

        Member member = memberRepository.findMemberById(memberId);

        List<Long> wishedStore = queryFactory.select(
                wishListStore.store.id)
            .from(wishListStore)
            .where(wishListStore.member.eq(member)
                .and(wishListStore.isDeleted.eq(false))
                .and(wishListStore.store.id.in(pageIds)))
            .fetch();

        updateLikeStatus(wishedStore, cursorPage);

        return cursorPage;
    }

    private static List<Long> getContentsIds(List<StoreResponseDto> cursorPage) {
        return cursorPage
            .stream()
            .map(StoreResponseDto::getStoreId)
            .toList();
    }

    private static void updateLikeStatus(
        List<Long> wishedIds,
        List<StoreResponseDto> cursorPage
    ) {
        for (Long id : wishedIds) {
            for (StoreResponseDto response : cursorPage) {
                if (id.equals(response.getStoreId())) {
                    response.isWishStore();
                }
            }
        }
    }

    private static boolean checkingHasNext(List<StoreResponseDto> responseDtos) {
        return responseDtos.size() >= PAGE_SIZE + 1;
    }

    private BooleanBuilder getCursorCondition(Long cursorId) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(store.isDeleted.eq(false));
        if (Objects.isNull(cursorId)) {
            return booleanBuilder;
        }
        Long startId = checkingExistence(cursorId);

        booleanBuilder.and(store.id.goe(startId));
        return booleanBuilder;
    }

    private Long checkingExistence(Long cursorId) {
        Long checkingId = queryFactory.select(store.id)
            .from(store)
            .where(store.id.eq(cursorId))
            .fetchOne();

        if (Objects.isNull(checkingId)) {
            throw new IllegalArgumentException("존재하지 않는 게시글 아이디입니다.");
        }

        return cursorId + 1;
    }

}
