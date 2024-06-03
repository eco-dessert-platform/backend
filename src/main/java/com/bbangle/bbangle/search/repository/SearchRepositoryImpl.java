package com.bbangle.bbangle.search.repository;

import static com.bbangle.bbangle.exception.BbangleErrorCode.UNKNOWN_CATEGORY;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.search.domain.QSearch;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.dto.request.SearchBoardRequest;
import com.bbangle.bbangle.search.dto.response.SearchBoardResponse;
import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.bbangle.bbangle.wishlist.domain.QWishListStore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchQueryDSLRepository {
    private final JPAQueryFactory queryFactory;
    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;
    private static final QStore store = QStore.store;
    private static final QWishListStore wishListStore = QWishListStore.wishListStore;
    private static final QWishListBoard wishListBoard = QWishListBoard.wishListBoard;
    private static final QSearch search = QSearch.search;
    private final int ONEDAY = 24;
    private final int DEFAULT_ITEM_SIZE = 10;


    // 빈 DTO 반환


    @Override
    public Long getSearchedBoardAllCount(SearchBoardRequest boardRequest, List<Long> boardIds){
        BooleanBuilder whereFilter = setFilteringCondition(boardRequest);

        return queryFactory
                .select(product.board.id)
                .distinct()
                .from(product)
                .where(
                        board.id.in(boardIds),
                        whereFilter
                ).fetchCount();
    }

    private SearchBoardResponse returnEmptyResponse(Pageable pageable, long total) {
        return SearchBoardResponse.getEmpty(pageable.getPageNumber(), DEFAULT_ITEM_SIZE, total);
    }

    // 정렬 기준 설정
    private OrderSpecifier<?> determineOrder(SearchBoardRequest boardRequest, List<Long> boardIds) {
        return boardRequest.sort().equals(SortType.POPULAR.name()) ?
                board.view.add(board.wishCnt.multiply(10)).desc() :
                orderByFieldList(boardIds, product.board.id);
    }

    // 페이징 적용된 조회
    private List<Long> fetchFilteredQuery(JPAQuery<Long> query, OrderSpecifier<?> orderBy, Pageable pageable) {
        return query.orderBy(orderBy).offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();
    }

    private OrderSpecifier<String> orderByFieldList(List<Long> boardIds, NumberPath<Long> id) {
        // 커스텀한 순서대로 데이터베이스 값을 뽑아올 수 있음
        String ids = boardIds.stream().map(String::valueOf).collect(Collectors.joining(", "));
        return Expressions.stringTemplate("FIELD({0}, " + ids + ")", id).asc();
    }

    public List<Long> getFilteredBoardIds(SearchBoardRequest boardRequest, List<Long> boardIds, Pageable pageable){
        BooleanBuilder whereFilter =
                setFilteringCondition(boardRequest);

        var defaultQuery = queryFactory
                .select(product.board.id)
                .distinct()
                .from(product)
                .where(
                        board.id.in(boardIds),
                        whereFilter
                );

        var orderBy = determineOrder(boardRequest, boardIds);
        return fetchFilteredQuery(defaultQuery, orderBy, pageable);
    }

    private List<Expression<?>> getColumnsForBoardDetails(Long memberId){
        List<Expression<?>> columns = new ArrayList<>();
        columns.add(product.board.store.id);
        columns.add(product.board.store.name);
        columns.add(product.board.id);
        columns.add(product.board.profile);
        columns.add(product.board.title);
        columns.add(product.board.price);
        columns.add(product.category);
        columns.add(product.glutenFreeTag);
        columns.add(product.highProteinTag);
        columns.add(product.sugarFreeTag);
        columns.add(product.veganTag);
        columns.add(product.ketogenicTag);

        if (memberId != null && memberId > 0) {
            columns.add(wishListBoard.id);
        }

        return columns;
    }

    public List<Tuple> fetchBoardDetailsByBoardIds(Long memberId, List<Long> filteredBoardIds){
        List<Expression<?>> columns = getColumnsForBoardDetails(memberId);

        var boards = queryFactory
                .select(columns.toArray(new Expression[0]))
                .from(product)
                .join(product.board, board)
                .join(product.board.store, store)
                .where(product.board.id.in(filteredBoardIds))
                .orderBy(orderByFieldList(filteredBoardIds, product.board.id));

        // 회원이라면 위시리스트 조인
        if (memberId != null && memberId > 0) {
            boards = boards.leftJoin(wishListBoard).on(wishListBoard.boardId.eq(board.id), wishListBoard.memberId.eq(memberId));
        }

        return boards.fetch();
    }

    private void putBoardResponseAtBoardIdToResponseMap(Map<Long, BoardResponseDto> boardIdToResponseMap, Tuple boardDetail, Long boardId, Set<Category> categories){
        boardIdToResponseMap.put(boardId,
                BoardResponseDto.builder()
                        .boardId(boardId)
                        .storeId(boardDetail.get(product.board.store.id))
                        .storeName(boardDetail.get(product.board.store.name))
                        .thumbnail(boardDetail.get(product.board.profile))
                        .title(boardDetail.get(product.board.title))
                        .price(boardDetail.get(product.board.price))
                        .isBundled(categories.size() > 1)
                        .tags(new ArrayList<>())
                        .isWished(false)
                        .build());

        categories.clear();
    }

    private BoardResponseDto getBoardTagsByboardDetailTuple(Map<Long, BoardResponseDto> boardIdToResponseMap, Tuple boardDetail, Long boardId){
        BoardResponseDto boardResponseDto = boardIdToResponseMap.get(boardDetail.get(product.board.id));

        if (boardDetail.get(product.glutenFreeTag)) {
            boardIdToResponseMap.get(boardId).getTags().add(TagEnum.GLUTEN_FREE.label());
        }
        if (boardDetail.get(product.highProteinTag)) {
            boardIdToResponseMap.get(boardId).getTags().add(TagEnum.HIGH_PROTEIN.label());
        }
        if (boardDetail.get(product.sugarFreeTag)) {
            boardIdToResponseMap.get(boardId).getTags().add(TagEnum.SUGAR_FREE.label());
        }
        if (boardDetail.get(product.veganTag)) {
            boardIdToResponseMap.get(boardId).getTags().add(TagEnum.VEGAN.label());
        }
        if (boardDetail.get(product.ketogenicTag)) {
            boardIdToResponseMap.get(boardId).getTags().add(TagEnum.KETOGENIC.label());
        }

        return boardResponseDto;
    }

    private List<BoardResponseDto> duplicateBoardResponse(Map<Long, BoardResponseDto> boardIdToResponseMap){
        return boardIdToResponseMap.entrySet().stream().map(
                longBoardResponseDtoEntry -> longBoardResponseDtoEntry.getValue()
        ).map(
                boardResponseDto -> removeDuplicatesFromDto(boardResponseDto)
        ).toList();
    }

    private List<BoardResponseDto> getResponseContentByBoardDetails(List<Tuple> boardDetails){
        Map<Long, BoardResponseDto> boardIdToResponseMap = new LinkedHashMap<>();
        Set<Category> categories = new HashSet<>();

        for (Tuple boardDetail:boardDetails) {
            Long boardId =  boardDetail.get(product.board.id);

            if (!boardIdToResponseMap.containsKey(boardId)) {
                putBoardResponseAtBoardIdToResponseMap(boardIdToResponseMap, boardDetail, boardId, categories);
            }
            categories.add(boardDetail.get(product.category));
            BoardResponseDto boardTags = getBoardTagsByboardDetailTuple(boardIdToResponseMap, boardDetail, boardId);
            boardIdToResponseMap.put(boardDetail.get(product.board.id), boardTags);
        }

        return duplicateBoardResponse(boardIdToResponseMap);
    }

    @Override
    public SearchBoardResponse getSearchedBoard(
            Long memberId, List<Long> boardIds, SearchBoardRequest boardRequest,
            Pageable pageable, Long searchedBoardAllCount
    ) {
        List<Long> filteredBoardIds = getFilteredBoardIds(boardRequest, boardIds, pageable);

        if (filteredBoardIds.size() <= 0){
            return returnEmptyResponse(pageable, searchedBoardAllCount);
        }

        List<Tuple> boardDetails = fetchBoardDetailsByBoardIds(memberId, filteredBoardIds);

        List<BoardResponseDto> content = getResponseContentByBoardDetails(boardDetails);

        return SearchBoardResponse.builder()
                .content(content)
                .pageNumber(pageable.getPageNumber())
                .itemAllCount(searchedBoardAllCount)
                .limitItemCount(DEFAULT_ITEM_SIZE)
                .currentItemCount(content.size())
                .existNextPage(searchedBoardAllCount - ((pageable.getPageNumber() + 1) * DEFAULT_ITEM_SIZE) > 0)
                .build();
    }

    @Override
    public List<StoreResponseDto> getSearchedStore(Long memberId, List<Long> storeIndexList, Pageable pageable){
        List<Expression<?>> columns = new ArrayList<>();
        columns.add(store.id);
        columns.add(store.name);
        columns.add(store.introduce);
        columns.add(store.profile);

        // 회원이라면 위시리스트 유무 확인
        if (memberId > 1L){
            columns.add(wishListStore.id);
        }

        var stores = queryFactory
                .select(columns.toArray(new Expression[0]))
                .from(store)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .where(store.id.in(storeIndexList))
                .orderBy(orderByFieldList(storeIndexList, store.id));

        // 회원이라면 위시리스트 테이블 Join
        if (memberId > 1L){
            stores.leftJoin(wishListStore).on(wishListStore.store.eq(store), wishListStore.member.id.eq(memberId), wishListStore.isDeleted.eq(false));
        }

        return stores.fetch().stream().map(
                tuple -> StoreResponseDto.builder()
                        .storeId(tuple.get(store.id))
                        .storeName(tuple.get(store.name))
                        .introduce(tuple.get(store.introduce))
                        .profile(tuple.get(store.profile))
                        .isWished(tuple.get(wishListStore.id)!=null?true:false)
                        .build()).toList();
    }



    private static BoardResponseDto removeDuplicatesFromDto(BoardResponseDto boardResponseDto) {
        List<String> uniqueTags = boardResponseDto.getTags().stream().distinct().collect(Collectors.toList());

        return BoardResponseDto.builder()
                .boardId(boardResponseDto.getBoardId())
                .storeId(boardResponseDto.getStoreId())
                .storeName(boardResponseDto.getStoreName())
                .thumbnail(boardResponseDto.getThumbnail())
                .title(boardResponseDto.getTitle())
                .price(boardResponseDto.getPrice())
                .isBundled(boardResponseDto.getIsBundled())
                .isWished(boardResponseDto.getIsWished())
                .tags(uniqueTags)
                .build();
    }

    @Override
    public List<KeywordDto> getRecencyKeyword(Member member) {
        return queryFactory.select(search.keyword, search.createdAt.max())
                .from(search)
                .where(search.isDeleted.eq(false), search.member.eq(member))
                .groupBy(search.keyword)
                .orderBy(search.createdAt.max().desc())
                .limit(7)
                .fetch().stream().map(tuple -> new KeywordDto(tuple.get(search.keyword)))
                .toList();
    }

    @Override
    public String[] getBestKeyword() {

        // 현재시간과 하루전 시간을 가져옴
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime beforeOneDayTime = currentTime.minusHours(ONEDAY);

        // 현재시간으로부터 24시간 전 검색어를 검색수 내림 차순으로 7개 가져옴
        return queryFactory.select(search.keyword)
                .from(search)
                .where(search.createdAt.gt(beforeOneDayTime))
                .groupBy(search.keyword)
                .orderBy(search.count().desc())
                .limit(7)
                .fetch()
                .toArray(new String[0]);
    }

    @Override
    public void markAsDeleted(String keyword, Member member) {
        QSearch search = QSearch.search;
        queryFactory.update(search)
                .set(search.isDeleted, true)
                .where(
                        search.member.eq(member)
                                .and(search.keyword.eq(keyword))
                )
                .execute();
    }

    private static BooleanBuilder setFilteringCondition(SearchBoardRequest request) {
        BooleanBuilder filterBuilder = new BooleanBuilder();
        if (request.glutenFreeTag() != null && request.glutenFreeTag() == true) {
            filterBuilder.and(product.glutenFreeTag.eq(request.glutenFreeTag()));
        }
        if (request.highProteinTag() != null && request.highProteinTag() == true) {
            filterBuilder.and(product.highProteinTag.eq(request.highProteinTag()));
        }
        if (request.sugarFreeTag() != null && request.sugarFreeTag() == true) {
            filterBuilder.and(product.sugarFreeTag.eq(request.sugarFreeTag()));
        }
        if (request.veganTag() != null && request.veganTag() == true) {
            filterBuilder.and(product.veganTag.eq(request.veganTag()));
        }
        if (request.ketogenicTag() != null && request.ketogenicTag() == true) {
            filterBuilder.and(product.ketogenicTag.eq(request.ketogenicTag()));
        }
        if (request.orderAvailableToday() != null && request.orderAvailableToday() == true) {
            LocalDate currentDate = LocalDate.now();
            String dayOfWeek = currentDate.getDayOfWeek().toString().substring(0, 3);
            switch (dayOfWeek){
                case "MON":
                    filterBuilder.and(board.monday.eq(true));
                    break;
                case "TUE":
                    filterBuilder.and(board.tuesday.eq(true));
                    break;
                case "WED":
                    filterBuilder.and(board.wednesday.eq(true));
                    break;
                case "THU":
                    filterBuilder.and(board.thursday.eq(true));
                    break;
                case "FRI":
                    filterBuilder.and(board.friday.eq(true));
                    break;
                case "SAT":
                    filterBuilder.and(board.saturday.eq(true));
                    break;
                case "SUN":
                    filterBuilder.and(board.sunday.eq(true));
                    break;
            }

        }
        if (request.category() != null && !request.category().isBlank()) {
            if (!Category.checkCategory(request.category())) {
                throw new BbangleException(UNKNOWN_CATEGORY);
            }
            filterBuilder.and(product.category.eq(Category.valueOf(request.category())));
        }

        if (request.minPrice() != null && request.minPrice()!=0) {
            filterBuilder.and(board.price.goe(request.minPrice()));
        }
        if (request.maxPrice() != null && request.maxPrice()!=0) {
            filterBuilder.and(board.price.loe(request.maxPrice()));
        }
        return filterBuilder;
    }
}
