package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.*;
import com.bbangle.bbangle.board.dto.*;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.wishListBoard.domain.QWishlistProduct;
import com.bbangle.bbangle.wishListFolder.domain.QWishlistFolder;
import com.bbangle.bbangle.wishListFolder.domain.WishlistFolder;
import com.bbangle.bbangle.wishListStore.domain.QWishlistStore;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.dsl.Expressions;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import com.bbangle.bbangle.util.SecurityUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import static com.bbangle.bbangle.exception.BbangleErrorCode.UNKNOWN_CATEGORY;
import static com.bbangle.bbangle.wishListBoard.domain.QWishlistProduct.wishlistProduct;

@Repository
@Slf4j
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardQueryDSLRepository {

    private static final int PAGE_SIZE = 10;

    private final JPAQueryFactory queryFactory;

    @Override
    public BoardCustomPage<List<BoardResponseDto>> getBoardResponseDto(
        FilterRequest filterRequest,
        List<Long> matchedIdx,
        Long cursorId
    ) {
        QBoard board = QBoard.board;
        QProduct product = QProduct.product;
        QStore store = QStore.store;

        BooleanBuilder filter =
            setFilteringCondition(
                filterRequest,
                product,
                board);

        BooleanBuilder cursorBuilder = setCursorBuilder(cursorId, matchedIdx, board);

        List<Board> boards = queryFactory
            .selectFrom(board)
            .leftJoin(board.productList, product)
            .fetchJoin()
            .leftJoin(board.store, store)
            .fetchJoin()
            .where(cursorBuilder.and(filter))
            .orderBy(orderByFieldList(board, matchedIdx))
            .limit(PAGE_SIZE + 1)
            .fetch();

        Map<Long, List<ProductTagDto>> productTagsByBoardId = getLongListMap(boards);
        boolean hasNext = isHasNext(boards);
        List<BoardResponseDto> content = getBoardResponseDtos(
            boards, productTagsByBoardId);

        return getBoardCustomPage(cursorId, board, product, filter, store, content, hasNext);
    }

    @Override
    public Slice<BoardResponseDto> getAllByFolder(
        String sort, Pageable pageable, Long wishListFolderId,
        WishlistFolder selectedFolder
    ) {
        QBoard board = QBoard.board;
        QProduct product = QProduct.product;
        QStore store = QStore.store;
        QWishlistProduct products = wishlistProduct;
        QWishlistFolder folder = QWishlistFolder.wishlistFolder;

        OrderSpecifier<?> orderSpecifier = sortTypeFolder(sort, board, products);

        List<Board> boards = queryFactory
            .selectFrom(board)
            .leftJoin(board.productList, product)
            .fetchJoin()
            .leftJoin(board.store, store)
            .fetchJoin()
            .join(board)
            .on(board.id.eq(products.board.id))
            .join(products)
            .on(products.wishlistFolder.eq(folder))
            .where(products.wishlistFolder.eq(selectedFolder)
                .and(products.isDeleted.eq(false)))
            .offset(pageable.getOffset())
            .orderBy(orderSpecifier)
            .limit(pageable.getPageSize() + 1)
            .fetch();

        Map<Long, List<ProductTagDto>> productTagsByBoardId = getLongListMap(boards);

        List<BoardResponseDto> content = new ArrayList<>();

        // isBundled 포함한 정리
        for (Board board1 : boards) {
            List<String> tags = addList(productTagsByBoardId.get(board1.getId()));
            content.add(BoardResponseDto.inFolder(board1, tags));
        }

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public BoardDetailResponseDto getBoardDetailResponse(Long memberId, Long boardId) {
        QBoard board = QBoard.board;
        QProduct product = QProduct.product;
        QStore store = QStore.store;
        QProductImg productImg = QProductImg.productImg;
        QBoardDetail boardDetail = QBoardDetail.boardDetail;

        QWishlistProduct wishlistProduct = QWishlistProduct.wishlistProduct;
        QWishlistStore wishlistStore = QWishlistStore.wishlistStore;

        List<Expression<?>> columns = new ArrayList<>();
        columns.add(store.id);
        columns.add(store.name);
        columns.add(store.profile);
        columns.add(board.id);
        columns.add(board.profile);
        columns.add(board.title);
        columns.add(board.price);
        columns.add(productImg.id);
        columns.add(productImg.url);
        columns.add(board.monday);
        columns.add(board.tuesday);
        columns.add(board.wednesday);
        columns.add(board.thursday);
        columns.add(board.friday);
        columns.add(board.saturday);
        columns.add(board.sunday);
        columns.add(board.purchaseUrl);
        columns.add(product.id);
        columns.add(product.title);
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

        var jpaQuery = queryFactory
            .select(columns.toArray(new Expression[0]))
            .from(product)
            .where(board.id.eq(boardId))
            .join(product.board, board)
            .join(board.store, store)
            .leftJoin(productImg)
            .on(board.id.eq(productImg.board.id));

        if (memberId != null && memberId > 0) {
            jpaQuery.leftJoin(wishlistProduct)
                .on(wishlistProduct.board.eq(board), wishlistProduct.memberId.eq(memberId),
                    wishlistProduct.isDeleted.eq(false))
                .leftJoin(wishlistStore)
                .on(wishlistStore.store.eq(store), wishlistStore.member.id.eq(memberId),
                    wishlistStore.isDeleted.eq(false));
            columns.add(wishlistProduct.id);
        }

        var fetch = jpaQuery.fetch();

        var boardDetails = queryFactory.select(new QDetailResponseDto(
                boardDetail.id,
                boardDetail.imgIndex,
                boardDetail.url
            ))
            .from(boardDetail)
            .where(board.id.eq(boardId))
            .stream()
            .toList();

        int index = 0;
        int resultSize = fetch.size();
        StoreDto storeDto = null;
        BoardDetailDto boardDto = null;
        List<ProductDto> productDtos = new ArrayList<>();
        Set<BoardImgDto> boardImgDtos = new HashSet<>();
        Set<String> allTags = new HashSet<>();
        Set<Category> categories = new HashSet<>();
        List<String> tags = new ArrayList<>();

        for (Tuple tuple : fetch) {
            index++;

            if (tuple.get(product.glutenFreeTag)) {
                tags.add(TagEnum.GLUTEN_FREE.label());
            }
            if (tuple.get(product.highProteinTag)) {
                tags.add(TagEnum.HIGH_PROTEIN.label());
            }
            if (tuple.get(product.sugarFreeTag)) {
                tags.add(TagEnum.SUGAR_FREE.label());
            }
            if (tuple.get(product.veganTag)) {
                tags.add(TagEnum.VEGAN.label());
            }
            if (tuple.get(product.ketogenicTag)) {
                tags.add(TagEnum.KETOGENIC.label());
            }
            categories.add(tuple.get(product.category));
            allTags.addAll(tags);

            boardImgDtos.add(
                BoardImgDto.builder()
                    .id(tuple.get(productImg.id))
                    .url(tuple.get(productImg.url))
                    .build()
            );

            productDtos.add(
                ProductDto.builder()
                    .id(tuple.get(product.id))
                    .title(tuple.get(product.title))
                    .category(tuple.get(product.category))
                    .tags(new ArrayList<>(tags))
                    .build()
            );

            tags.clear();

            if (index == resultSize) {
                storeDto = StoreDto.builder()
                    .storeId(tuple.get(store.id))
                    .storeName(tuple.get(store.name))
                    .profile(tuple.get(store.profile))
                    .isWished(tuple.get(wishlistStore.id) != null ? true : false)
                    .build();

                boardDto = BoardDetailDto.builder()
                    .boardId(tuple.get(board.id))
                    .thumbnail(tuple.get(board.profile))
                    .title(tuple.get(board.title))
                    .price(tuple.get(board.price))
                    .orderAvailableDays(
                        BoardAvailableDayDto.builder()
                            .mon(tuple.get(board.monday))
                            .tue(tuple.get(board.tuesday))
                            .wed(tuple.get(board.wednesday))
                            .thu(tuple.get(board.thursday))
                            .fri(tuple.get(board.friday))
                            .sat(tuple.get(board.saturday))
                            .sun(tuple.get(board.sunday))
                            .build()
                    )
                    .purchaseUrl(tuple.get(board.purchaseUrl))
                    .detail(boardDetails)
                    .products(productDtos)
                    .images(boardImgDtos.stream()
                        .toList())
                    .tags(allTags.stream()
                        .toList())
                    .isWished(tuple.get(wishlistProduct.id) != null ? true : false)
                    .isBundled(categories.size() > 1)
                    .build();
            }
        }

        return BoardDetailResponseDto.builder()
            .store(storeDto)
            .board(boardDto)
            .build();
    }

    @Override
    public HashMap<Long, String> getAllBoardTitle() {
        QBoard board = QBoard.board;

        List<Tuple> fetch = queryFactory
            .select(board.id, board.title)
            .from(board)
            .fetch();

        HashMap<Long, String> boardMap = new HashMap<>();
        fetch.forEach((tuple) -> boardMap.put(tuple.get(board.id), tuple.get(board.title)));

        return boardMap;
    }

    @Override
    public List<BoardResponseDto> updateLikeStatus(
        List<Long> matchedIdx,
        List<BoardResponseDto> content
    ) {
        QBoard board = QBoard.board;
        QWishlistProduct wishlistProduct = QWishlistProduct.wishlistProduct;

        Long memberId = SecurityUtils.getMemberId();

        BooleanExpression isLikedExpression = wishlistProduct.isDeleted.isFalse();

        List<ProductBoardLikeStatus> likeFetch = queryFactory
            .select(Projections.bean(
                ProductBoardLikeStatus.class,
                board.id.as("boardId"),
                isLikedExpression.as("isWished")
            ))
            .from(board)
            .leftJoin(wishlistProduct)
            .on(board.id.eq(wishlistProduct.board.id)
                .and(wishlistProduct.memberId.eq(memberId)))
            .where(board.id.in(matchedIdx))
            .fetch()
            .stream()
            .peek(result -> {
                if (result.getIsLike() == null) {
                    result.setIsLike(false);
                }
            })
            .toList();

        for (ProductBoardLikeStatus likeStatus : likeFetch) {
            if (likeStatus.getIsLike()) {
                for (BoardResponseDto boardResponseDto : content) {
                    if (Objects.equals(likeStatus
                        .getBoardId(), boardResponseDto
                        .boardId())) {
                        boardResponseDto
                            .updateLike(true);
                    }
                }
            }
        }
        return content;
    }

    private BooleanBuilder setCursorBuilder(Long cursorId, List<Long> matchedIdx, QBoard board) {
        BooleanBuilder cursorBuilder = new BooleanBuilder();
        if (Objects.nonNull(cursorId)) {
            Long startId = returnStartId(cursorId, matchedIdx);
            cursorBuilder.and(board.id.loe(startId));
        }

        return cursorBuilder;
    }

    private BoardCustomPage<List<BoardResponseDto>> getBoardCustomPage(
        Long cursorId,
        QBoard board,
        QProduct product,
        BooleanBuilder filter,
        QStore store,
        List<BoardResponseDto> content,
        boolean hasNext
    ) {
        if(Objects.isNull(cursorId)){
            Long boardCnt = queryFactory
                .select(board.countDistinct())
                .from(store)
                .join(board).on(board.store.eq(store))
                .join(product)
                .on(product.board.eq(board))
                .where(filter)
                .fetchOne();

            if(Objects.isNull(boardCnt)){
                boardCnt = 0L;
            }

            Long storeCnt = queryFactory
                .select(store.countDistinct())
                .from(store)
                .join(board).on(board.store.eq(store))
                .join(product)
                .on(product.board.eq(board))
                .where(filter)
                .fetchOne();

            if(Objects.isNull(storeCnt)){
                storeCnt = 0L;
            }

            return BoardCustomPage.from(content, 0L, hasNext, boardCnt, storeCnt);
        }
        return BoardCustomPage.from(content, cursorId, hasNext);
    }

    private Long returnStartId(Long cursorId, List<Long> rankIds) {
        if (Objects.isNull(cursorId)) {
            return rankIds.get(0);
        }

        return cursorId + 1L;
    }

    private List<BoardResponseDto> getBoardResponseDtos(
        List<Board> boards,
        Map<Long, List<ProductTagDto>> productTagsByBoardId
    ) {
        List<BoardResponseDto> content = new ArrayList<>();
        for (int i = 0; i < boards.size(); i++) {
            if (i == PAGE_SIZE) {
                continue;
            }
            List<String> tags = addList(productTagsByBoardId.get(boards.get(i)
                .getId()));
            content.add(BoardResponseDto.from(boards.get(i), tags));
        }
        return content;
    }

    private boolean isHasNext(List<Board> boards) {
        return boards.size() >= PAGE_SIZE + 1;
    }

    private static OrderSpecifier<?> sortTypeFolder(
        String sort,
        QBoard board,
        QWishlistProduct products
    ) {
        OrderSpecifier<?> orderSpecifier;
        if (sort == null) {
            orderSpecifier = products.createdAt.desc();
            return orderSpecifier;
        }
        switch (SortType.fromString(sort)) {
            case RECENT:
                orderSpecifier = products.createdAt.desc();
                break;
            case LOW_PRICE:
                orderSpecifier = board.price.asc();
                break;
            case POPULAR:
                orderSpecifier = board.wishCnt.desc();
                break;
            default:
                throw new BbangleException("Invalid SortType");
        }
        return orderSpecifier;
    }

    private static Map<Long, List<ProductTagDto>> getLongListMap(List<Board> boards) {
        Map<Long, List<ProductTagDto>> productTagsByBoardId = new HashMap<>();
        for (Board board1 : boards) {
            for (Product product1 : board1.getProductList()) {
                productTagsByBoardId.put(board1.getId(),
                    productTagsByBoardId.getOrDefault(board1.getId(), new ArrayList<>()));
                productTagsByBoardId.get(board1.getId())
                    .add(ProductTagDto.from(product1));
            }
        }
        return productTagsByBoardId;
    }

    private OrderSpecifier<?> orderByFieldList(QBoard board, List<Long> boardId) {
        String boardIdStr = boardId.stream()
            .map(Object::toString)
            .collect(Collectors.joining(", "));

        String template = String.format("FIELD(%s, %s)", board.id, boardIdStr);

        return new OrderSpecifier<>(Order.ASC, Expressions.stringTemplate(template));
    }

    private static BooleanBuilder setFilteringCondition(
        FilterRequest filterRequest,
        QProduct product,
        QBoard board
    ) {

        BooleanBuilder filterBuilder = new BooleanBuilder();
        if (filterRequest.glutenFreeTag() != null) {
            filterBuilder.and(product.glutenFreeTag.eq(filterRequest.glutenFreeTag()));
        }
        if (filterRequest.highProteinTag() != null) {
            filterBuilder.and(product.highProteinTag.eq(filterRequest.highProteinTag()));
        }
        if (filterRequest.sugarFreeTag() != null) {
            filterBuilder.and(product.sugarFreeTag.eq(filterRequest.sugarFreeTag()));
        }
        if (filterRequest.veganTag() != null) {
            filterBuilder.and(product.veganTag.eq(filterRequest.veganTag()));
        }
        if (filterRequest.ketogenicTag() != null) {
            filterBuilder.and(product.ketogenicTag.eq(filterRequest.ketogenicTag()));
        }
        if (filterRequest.category() != null) {
//            if (!Category.checkCategory(filterRequest.category())) {
//                throw new BbangleException(UNKNOWN_CATEGORY);
//            }
            filterBuilder.and(product.category.eq(filterRequest.category()));
        }
        if (filterRequest.minPrice() != null) {
            filterBuilder.and(board.price.goe(filterRequest.minPrice()));
        }
        if (filterRequest.maxPrice() != null) {
            filterBuilder.and(board.price.loe(filterRequest.maxPrice()));
        }
        if (filterRequest.orderAvailableToday() != null && filterRequest.orderAvailableToday()) {
            DayOfWeek dayOfWeek = LocalDate.now()
                .getDayOfWeek();

            switch (dayOfWeek) {
                case MONDAY -> filterBuilder.and(board.monday.eq(true));
                case TUESDAY -> filterBuilder.and(board.tuesday.eq(true));
                case WEDNESDAY -> filterBuilder.and(board.wednesday.eq(true));
                case THURSDAY -> filterBuilder.and(board.thursday.eq(true));
                case FRIDAY -> filterBuilder.and(board.friday.eq(true));
                case SATURDAY -> filterBuilder.and(board.saturday.eq(true));
                case SUNDAY -> filterBuilder.and(board.sunday.eq(true));
            }
        }
        return filterBuilder;
    }

    private List<String> addList(List<ProductTagDto> dtos) {
        List<String> tags = new ArrayList<>();
        boolean glutenFreeTag = false;
        boolean highProteinTag = false;
        boolean sugarFreeTag = false;
        boolean veganTag = false;
        boolean ketogenicTag = false;
        if (dtos == null) {
            return tags;
        }
        for (ProductTagDto dto : dtos) {
            if (dto.glutenFreeTag()) {
                glutenFreeTag = true;
            }
            if (dto.highProteinTag()) {
                highProteinTag = true;
            }
            if (dto.sugarFreeTag()) {
                sugarFreeTag = true;
            }
            if (dto.veganTag()) {
                veganTag = true;
            }
            if (dto.ketogenicTag()) {
                ketogenicTag = true;
            }
        }
        if (glutenFreeTag) {
            tags.add(TagEnum.GLUTEN_FREE.label());
        }
        if (highProteinTag) {
            tags.add(TagEnum.HIGH_PROTEIN.label());
        }
        if (sugarFreeTag) {
            tags.add(TagEnum.SUGAR_FREE.label());
        }
        if (veganTag) {
            tags.add(TagEnum.VEGAN.label());
        }
        if (ketogenicTag) {
            tags.add(TagEnum.KETOGENIC.label());
        }
        return tags;
    }

}
