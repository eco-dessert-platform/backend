package com.bbangle.bbangle.search.repository;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.exception.CategoryTypeException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.search.domain.QSearch;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.dto.request.SearchBoardRequest;
import com.bbangle.bbangle.search.dto.response.SearchBoardResponse;
import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import com.bbangle.bbangle.wishListBoard.domain.QWishlistProduct;
import com.bbangle.bbangle.wishListStore.domain.QWishlistStore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchQueryDSLRepository {
    private final JPAQueryFactory queryFactory;
    private final int ONEDAY = 24;
    private final int DEFAULT_ITEM_SIZE = 10;



    @Override
    public SearchBoardResponse getSearchedBoard(Long memberId, List<Long> boardIds, SearchBoardRequest boardRequest, Pageable pageable) {
        QBoard board = QBoard.board;
        QProduct product = QProduct.product;
        QStore store = QStore.store;

        QWishlistProduct wishlistProduct = QWishlistProduct.wishlistProduct;

        BooleanBuilder filter =
                setFilteringCondition(boardRequest.glutenFreeTag(),
                        boardRequest.highProteinTag(),
                        boardRequest.sugarFreeTag(),
                        boardRequest.veganTag(),
                        boardRequest.ketogenicTag(),
                        boardRequest.orderAvailableToday(),
                        boardRequest.category(),
                        boardRequest.minPrice(),
                        boardRequest.maxPrice(),
                        product,
                        board);




        var defaultQuery = queryFactory
                .select(product.board.id)
                .distinct()
                .from(product)
                .where(

                        board.id.in(boardIds),
                        filter
                );


        // 검색된 게시물 전체 개수
        var queryAllCount = defaultQuery.fetch().stream().toList().size();

        // 게시글이 없다면 빈 DTO 반환
        if (queryAllCount <= 0){
            return SearchBoardResponse.getEmpty(pageable.getPageNumber(), DEFAULT_ITEM_SIZE, queryAllCount);
        }

        // 인가 상품인 경우
        // 조회수 1점, 위시리스트 10점 점수가 가장 높은 순으로 정렬
        var orderByBuilder =
                boardRequest.sort().equals(SortType.POPULAR.getValue())?
                        board.view.add(board.wishCnt.multiply(10)).desc():
                        orderByFieldList(boardIds, product.board.id);


        // 필터링된 board의 id를 10개의 데이터를 끊어서 가져옴
        var filterQuery = defaultQuery
                .orderBy(orderByBuilder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (filterQuery.size() <= 0){
            return SearchBoardResponse.getEmpty(pageable.getPageNumber(), DEFAULT_ITEM_SIZE, queryAllCount);
        }

        // 가져올 컬럼명 입력
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

        // 회원이라면 위시리스트 등록 여부도 파악
        if (memberId != null && memberId > 0) {
            columns.add(wishlistProduct.id);
        }

        // 위에서 찾은 10개 이하의 게시판의 정보들을 가져오는 쿼리 작성
        var boards = queryFactory
                .select(columns.toArray(new Expression[0]))
                .from(product)
                .join(product.board, board)
                .join(product.board.store, store)
                .where(product.board.id.in(filterQuery))
                .orderBy(orderByFieldList(filterQuery, product.board.id));

        // 회원이라면 위시리스트 조인
        if (memberId != null && memberId > 0) {
            boards = boards.leftJoin(wishlistProduct).on(wishlistProduct.board.eq(board), wishlistProduct.memberId.eq(memberId), wishlistProduct.isDeleted.eq(false));
        }


        Map<Long, BoardResponseDto> boardMap = new LinkedHashMap<>();
        Set<Category> categories = new HashSet<>();

        for (Tuple tuple:boards.fetch()) {
            Long boardId =  tuple.get(product.board.id);

            // 이전 게시판 아이디와 현재 게시판 아이디가 다르다면 Map에 저장
            if (!boardMap.containsKey(boardId)) {
                boardMap.put(boardId,
                        BoardResponseDto.builder()
                                .boardId(boardId)
                                .storeId(tuple.get(product.board.store.id))
                                .storeName(tuple.get(product.board.store.name))
                                .thumbnail(tuple.get(product.board.profile))
                                .title(tuple.get(product.board.title))
                                .price(tuple.get(product.board.price))
                                .isBundled(categories.size() > 1)
                                .tags(new ArrayList<>())
                                .isWished(false)
                                .build());

                categories.clear();
            }

            // 묶음 상품 표시를 위해 카테고리 SetMap에 저장
            // 중복값은 저절로 제거됨
            categories.add(tuple.get(product.category));

            // 게시판에 표시할 전체태그 구성
            BoardResponseDto boardResponseDto = boardMap.get(tuple.get(product.board.id));

            if (tuple.get(product.glutenFreeTag)) {
                boardMap.get(boardId).tags().add(TagEnum.GLUTEN_FREE.label());
            }
            if (tuple.get(product.highProteinTag)) {
                boardMap.get(boardId).tags().add(TagEnum.HIGH_PROTEIN.label());
            }
            if (tuple.get(product.sugarFreeTag)) {
                boardMap.get(boardId).tags().add(TagEnum.SUGAR_FREE.label());
            }
            if (tuple.get(product.veganTag)) {
                boardMap.get(boardId).tags().add(TagEnum.VEGAN.label());
            }
            if (tuple.get(product.ketogenicTag)) {
                boardMap.get(boardId).tags().add(TagEnum.KETOGENIC.label());
            }

            boardMap.put(tuple.get(product.board.id), boardResponseDto);
        }

        // DTO 중복제거
        var content = boardMap.entrySet().stream().map(
                longBoardResponseDtoEntry -> longBoardResponseDtoEntry.getValue()
        ).map(
                boardResponseDto -> removeDuplicatesFromDto(boardResponseDto)
        ).toList();

        return SearchBoardResponse.builder()
                .content(content)
                .pageNumber(pageable.getPageNumber())
                .itemAllCount(queryAllCount)
                .limitItemCount(DEFAULT_ITEM_SIZE)
                .currentItemCount(content.size())
                .existNextPage(queryAllCount - ((pageable.getPageNumber() + 1) * DEFAULT_ITEM_SIZE) > 0)
                .build();
    }
    private OrderSpecifier<?> orderByFieldList(List<Long> boardIds, NumberPath<Long> id) {
        // 커스텀한 순서대로 데이터베이스 값을 뽑아올 수 있음
        String ids = boardIds.stream().map(String::valueOf).collect(Collectors.joining(", "));
        return Expressions.stringTemplate("FIELD({0}, " + ids + ")", id).asc();
    }

    @Override
    public List<StoreResponseDto> getSearchedStore(Long memberId, List<Long> storeIndexList, Pageable pageable){
        QStore store = QStore.store;
        QWishlistStore wishlistStore = QWishlistStore.wishlistStore;


        List<Expression<?>> columns = new ArrayList<>();
        columns.add(store.id);
        columns.add(store.name);
        columns.add(store.introduce);
        columns.add(store.profile);

        // 회원이라면 위시리스트 유무 확인
        if (memberId > 1L){
            columns.add(wishlistStore.id);
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
            stores.leftJoin(wishlistStore).on(wishlistStore.store.eq(store), wishlistStore.member.id.eq(memberId), wishlistStore.isDeleted.eq(false));
        }

        return stores.fetch().stream().map(
                tuple -> StoreResponseDto.builder()
                        .storeId(tuple.get(store.id))
                        .storeName(tuple.get(store.name))
                        .introduce(tuple.get(store.introduce))
                        .profile(tuple.get(store.profile))
                        .isWished(tuple.get(wishlistStore.id)!=null?true:false)
                        .build()).toList();
    }



    private static BoardResponseDto removeDuplicatesFromDto(BoardResponseDto boardResponseDto) {
        List<String> uniqueTags = boardResponseDto.tags().stream().distinct().collect(Collectors.toList());

        return BoardResponseDto.builder()
                .boardId(boardResponseDto.boardId())
                .storeId(boardResponseDto.storeId())
                .storeName(boardResponseDto.storeName())
                .thumbnail(boardResponseDto.thumbnail())
                .title(boardResponseDto.title())
                .price(boardResponseDto.price())
                .isBundled(boardResponseDto.isBundled())
                .isWished(boardResponseDto.isWished())
                .tags(uniqueTags)
                .build();
    }

    @Override
    public List<KeywordDto> getRecencyKeyword(Member member) {
        QSearch search = QSearch.search;

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
        QSearch search = QSearch.search;

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

    private static BooleanBuilder setFilteringCondition(Boolean glutenFreeTag, Boolean highProteinTag,
                                                        Boolean sugarFreeTag, Boolean veganTag, Boolean ketogenicTag,
                                                        Boolean orderAvailableToday, String category,
                                                        Integer minPrice, Integer maxPrice,
                                                        QProduct product, QBoard board) {
        BooleanBuilder filterBuilder = new BooleanBuilder();
        if (glutenFreeTag != null && glutenFreeTag == true) {
            filterBuilder.and(product.glutenFreeTag.eq(glutenFreeTag));
        }
        if (highProteinTag != null && highProteinTag == true) {
            filterBuilder.and(product.highProteinTag.eq(highProteinTag));
        }
        if (sugarFreeTag != null && sugarFreeTag == true) {
            filterBuilder.and(product.sugarFreeTag.eq(sugarFreeTag));
        }
        if (veganTag != null && veganTag == true) {
            filterBuilder.and(product.veganTag.eq(veganTag));
        }
        if (ketogenicTag != null && ketogenicTag == true) {
            filterBuilder.and(product.ketogenicTag.eq(ketogenicTag));
        }
        if (orderAvailableToday != null && orderAvailableToday == true) {
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
        if (category != null && !category.isBlank()) {
            if (!Category.checkCategory(category)) {
                throw new CategoryTypeException();
            }
            filterBuilder.and(product.category.eq(Category.valueOf(category)));
        }

        if (minPrice != null && minPrice!=0) {
            filterBuilder.and(board.price.goe(minPrice));
        }
        if (maxPrice != null && maxPrice!=0) {
            filterBuilder.and(board.price.loe(maxPrice));
        }
        return filterBuilder;
    }
}
