package com.bbangle.bbangle.store.repository;

import static com.bbangle.bbangle.exception.BbangleErrorCode.STORE_NOT_FOUND;
import static com.bbangle.bbangle.ranking.domain.QRanking.ranking;

import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.page.StoreDetailCustomPage;
import com.bbangle.bbangle.store.dto.PopularBoardDto;
import com.bbangle.bbangle.store.dto.PopularBoardResponse;
import com.bbangle.bbangle.store.dto.QPopularBoardDto;
import com.bbangle.bbangle.store.dto.QStoreBoardListDto;
import com.bbangle.bbangle.store.dto.QStoreDetailStoreDto;
import com.bbangle.bbangle.store.dto.QStoreDto;
import com.bbangle.bbangle.store.dto.QStoreResponseDto;
import com.bbangle.bbangle.store.dto.StoreBoardsResponse;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.StoreCustomPage;
import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.store.dto.StoreDetailBoardDto;
import com.bbangle.bbangle.store.dto.StoreDetailProductDto;
import com.bbangle.bbangle.store.dto.StoreBoardListDto;
import com.bbangle.bbangle.store.dto.StoreDetailStoreDto;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.store.dto.StoreResponse;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.bbangle.bbangle.wishlist.domain.QWishListStore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreQueryDSLRepository {

    private static final Long PAGE_SIZE = 20L;
    private static final Long EMPTY_PAGE_CURSOR = -1L;
    private static final Boolean EMPTY_PAGE_HAS_NEXT = false;
    private final QStore store = QStore.store;
    private final QBoard board = QBoard.board;
    private final QProduct product = QProduct.product;
    private final QWishListStore wishListStore = QWishListStore.wishListStore;
    private final QWishListBoard wishListBoard = QWishListBoard.wishListBoard;
    private final JPAQueryFactory queryFactory;
    private final MemberRepository memberRepository;
    private final Expression<Long> emptywishListBoardNumber = Expressions.constant(-1L);
    public final int SINGLE_CATEGORY_COUNT = 1;


    private void checkWishlistBoard(JPAQuery<PopularBoardDto> query, Long memberId) {
        query.leftJoin(wishListBoard)
            .on(
                wishListBoard.memberId.eq(memberId),
                wishListBoard.boardId.eq(board.id));
    }

    private void getWishlistStoreJoinQuery(
        JPAQuery<StoreDetailStoreDto> query, Long memberId) {
        query.leftJoin(wishListStore)
            .on(wishListStore.store.eq(store),
                wishListStore.member.id.eq(memberId),
                wishListStore.isDeleted.eq(false));
    }

    private StoreDetailStoreDto getStoreDetailStoreDto(Long memberId, Long storeId) {
        Expression<Long> conditionalWhislistStore =
            Objects.nonNull(memberId) ? wishListStore.id : emptywishListBoardNumber;

        JPAQuery<StoreDetailStoreDto> storeDetailStoreDto = queryFactory.select(
                new QStoreDetailStoreDto(
                    store.id,
                    store.profile,
                    store.name,
                    store.introduce,
                    conditionalWhislistStore
                )
            ).from(store)
            .where(store.id.eq(storeId));

        if (Objects.nonNull(memberId)) {
            getWishlistStoreJoinQuery(storeDetailStoreDto, memberId);
        }

        return storeDetailStoreDto.fetchFirst();
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
    public StoreResponse getStoreResponse(Long memberId, Long storeId) {
        StoreDetailStoreDto storeDetailStoreDto = getStoreDetailStoreDto(memberId, storeId);

        return storeDetailStoreDto.toStoreResponse();
    }

    private List<Long> getTopThreeBoardIds(Long storeId) {
        return queryFactory.select(ranking.id)
            .from(ranking)
            .join(ranking.board, board)
            .where(board.store.id.eq(storeId))
            .limit(3)
            .fetch();
    }

    private List<PopularBoardDto> getPopularBoardDtoList(List<Long> topBoardIds,
        Long memberId) {
        Expression<Long> conditionalWhislistProduct =
            Objects.nonNull(memberId) ? wishListBoard.id : emptywishListBoardNumber;

        JPAQuery<PopularBoardDto> queryBeforeFetch = queryFactory
            .select(
                new QPopularBoardDto(
                    ranking.id,
                    board.id,
                    board.profile,
                    board.title,
                    board.price,
                    product.category,
                    conditionalWhislistProduct))
            .distinct()
            .from(ranking)
            .join(product).on(ranking.board.eq(product.board))
            .join(board).on(product.board.eq(board))
            .where(ranking.id.in(topBoardIds))
            .orderBy(ranking.board.id.desc());

        Optional.ofNullable(memberId).ifPresent(mId -> checkWishlistBoard(queryBeforeFetch, mId));

        return queryBeforeFetch.fetch();
    }

    private List<PopularBoardResponse> toPopularBoardResponse(
        List<PopularBoardDto> PopularBoardDtos) {
        return PopularBoardDtos.stream()
            .map(PopularBoardDto::toPopularBoardResponse)
            .toList();
    }

    private Map<Long, Long> countDuplicationBoardDto(
        List<PopularBoardResponse> popularBoardResponses) {
        return popularBoardResponses.stream()
            .collect(
                Collectors.groupingBy(PopularBoardResponse::getBoardId,
                    Collectors.counting()));
    }

    private List<PopularBoardResponse> markDuplicatesAsBundled(
        List<PopularBoardResponse> popularBoardResponses,
        Map<Long, Long> idCount) {
        List<PopularBoardResponse> duplicatedPopularBoardResponses = popularBoardResponses.stream()
            .distinct()
            .toList();

        duplicatedPopularBoardResponses.stream()
            .filter(popularBoardDto -> idCount.get(popularBoardDto.getBoardId()) > SINGLE_CATEGORY_COUNT)
            .forEach(popularBoardDto -> popularBoardDto.setWishlist(true));

        return duplicatedPopularBoardResponses;
    }

    @Override
    public List<PopularBoardResponse> getPopularBoardResponses(Long memberId, Long storeId) {
        List<Long> topBoardIds = getTopThreeBoardIds(storeId);

        if (Objects.isNull(topBoardIds)) {
            throw new BbangleException(STORE_NOT_FOUND);
        }

        List<PopularBoardDto> PopularBoardDtos = getPopularBoardDtoList(
            topBoardIds, memberId);

        List<PopularBoardResponse> popularBoardResponses = toPopularBoardResponse(PopularBoardDtos);
        Map<Long, Long> idCount = countDuplicationBoardDto(popularBoardResponses);
        return markDuplicatesAsBundled(popularBoardResponses, idCount);
    }

    private List<Long> getCursorIdToBoardIds(Long boardIdAsCursorId, Long storeId) {
        BooleanBuilder cursorCondition = getBoardCursorCondition(boardIdAsCursorId);

        return queryFactory
            .select(board.id)
            .from(board)
            .where(
                board.store.id.eq(storeId),
                cursorCondition)
            .limit(PAGE_SIZE + 1)
            .fetch();
    }

    private JPAQuery<StoreBoardListDto> getConditionalWishlistJoinQuery(
        JPAQuery<StoreBoardListDto> query, Long memberId) {
        return Objects.isNull(memberId) ? query :
            query.leftJoin(wishListBoard)
                .on(wishListBoard.boardId.eq(board.id),
                    wishListBoard.memberId.eq(memberId));
    }

    private List<String> toTags(List<StoreDetailProductDto> storeDetailProductDtos) {
        return storeDetailProductDtos.stream()
            .map(StoreDetailProductDto::toTags)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

    private boolean toIsBundled(List<StoreDetailProductDto> storeDetailProductDtos) {
        return storeDetailProductDtos.stream()
            .map(StoreDetailProductDto::category)
            .distinct()
            .count() > SINGLE_CATEGORY_COUNT;
    }

    private List<StoreBoardListDto> getStoreDetailSelectDtos(List<Long> cursorIdToBoardIds,
        Long memberId) {
        Expression<Long> conditionalWhislistProduct =
            Objects.nonNull(memberId) ? wishListBoard.id : emptywishListBoardNumber;

        JPAQuery<StoreBoardListDto> queryBeforeFetch = queryFactory.select(
                new QStoreBoardListDto(
                    board.id,
                    board.profile,
                    board.title,
                    board.price,
                    board.view,
                    product.id,
                    product.glutenFreeTag,
                    product.highProteinTag,
                    product.sugarFreeTag,
                    product.veganTag,
                    product.ketogenicTag,
                    product.category,
                    conditionalWhislistProduct))
            .from(product)
            .join(product.board, board)
            .where(board.id.in(cursorIdToBoardIds))
            .orderBy(board.id.desc());

        Optional.ofNullable(memberId)
            .ifPresent(mId -> getConditionalWishlistJoinQuery(queryBeforeFetch, mId));

        return queryBeforeFetch.fetch();
    }

    private BooleanBuilder getBoardCursorCondition(Long cursorId) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (Objects.isNull(cursorId)) {
            return booleanBuilder;
        }
        Long boardId = checkingBoardExistence(cursorId);

        booleanBuilder.and(board.id.goe(boardId));
        return booleanBuilder;
    }

    private Long checkingBoardExistence(Long cursorId) {
        Long checkingId = queryFactory.select(board.id)
            .from(board)
            .where(board.id.eq(cursorId))
            .fetchOne();

        if (Objects.isNull(checkingId)) {
            throw new IllegalArgumentException("존재하지 않는 게시글 아이디입니다.");
        }

        return cursorId + 1;
    }

    public Map<StoreDetailBoardDto, List<StoreDetailProductDto>> groupProductsByBoardId(
        List<StoreBoardListDto> storeDetailSelectDtos) {
        return storeDetailSelectDtos.stream()
            .collect(Collectors.groupingBy(
                StoreBoardListDto::toBoardDto,
                Collectors.mapping(StoreBoardListDto::toProductDto, Collectors.toList())
            ));
    }

    private static boolean checkingHasNextStoreDetail(
        List<StoreBoardsResponse> storeDetailResponse) {
        return storeDetailResponse.size() >= PAGE_SIZE + 1;
    }

    private List<StoreBoardsResponse> toStoreBoardsResponse(
        Map<StoreDetailBoardDto, List<StoreDetailProductDto>> productsByBoardId) {
        List<StoreBoardsResponse> storeBoardsResponses = new ArrayList<>();
        productsByBoardId.forEach((storeDetailBoardDto, storeDetailProductDtos) -> {
            List<String> tags = toTags(storeDetailProductDtos);
            Boolean isBundled = toIsBundled(storeDetailProductDtos);

            storeBoardsResponses.add(
                StoreBoardsResponse.builder()
                    .boardId(storeDetailBoardDto.boardId())
                    .boardThumbnail(storeDetailBoardDto.boardProfile())
                    .boardTitle(storeDetailBoardDto.boardTitle())
                    .view(storeDetailBoardDto.boardView())
                    .tags(tags)
                    .isBundled(isBundled)
                    .boardPrice(storeDetailBoardDto.boardPrice())
                    .isWished(storeDetailBoardDto.isWished())
                    .build());
        });

        return storeBoardsResponses;
    }

    @Override
    public StoreDetailCustomPage<List<StoreBoardsResponse>> getStoreBoardsResponse(
        Long memberId,
        Long storeId,
        Long boardIdAsCursorId
    ) {
        List<Long> cursorIdToBoardIds = getCursorIdToBoardIds(boardIdAsCursorId, storeId);
        List<StoreBoardListDto> storeDetailSelectDtos = getStoreDetailSelectDtos(
            cursorIdToBoardIds, memberId);
        Map<StoreDetailBoardDto, List<StoreDetailProductDto>> productsByBoardId = groupProductsByBoardId(
            storeDetailSelectDtos);
        List<StoreBoardsResponse> storeBoardsResponses = toStoreBoardsResponse(productsByBoardId);

        boolean hasNext = checkingHasNextStoreDetail(storeBoardsResponses);

        if (hasNext) {
            storeBoardsResponses.remove(storeBoardsResponses.get(storeBoardsResponses.size() - 1));
        }

        return StoreDetailCustomPage.from(storeBoardsResponses, boardIdAsCursorId, hasNext);
    }

    @Override
    public HashMap<Long, String> getAllStoreTitle() {
        List<Tuple> fetch = queryFactory
            .select(store.id, store.name)
            .from(store)
            .fetch();

        HashMap<Long, String> storeMap = new HashMap<>();
        fetch.forEach((tuple) -> storeMap.put(tuple.get(store.id), tuple.get(store.name)));

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
