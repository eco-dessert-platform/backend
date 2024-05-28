package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QBoardDetail;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.domain.QProductImg;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.BoardAvailableDayDto;
import com.bbangle.bbangle.board.dto.BoardDetailDto;
import com.bbangle.bbangle.board.dto.BoardDetailResponse;
import com.bbangle.bbangle.board.dto.BoardDetailSelectDto;
import com.bbangle.bbangle.board.dto.BoardImgDto;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.CursorInfo;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.dto.ProductDto;
import com.bbangle.bbangle.board.dto.QBoardDetailDto;
import com.bbangle.bbangle.board.repository.folder.cursor.BoardInFolderCursorGeneratorMapping;
import com.bbangle.bbangle.board.repository.folder.query.BoardInFolderQueryGeneratorMapping;
import com.bbangle.bbangle.board.repository.query.BoardQueryProviderResolver;
import com.bbangle.bbangle.common.sort.FolderBoardSortType;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.ranking.domain.QRanking;
import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.bbangle.bbangle.wishlist.domain.QWishListFolder;
import com.bbangle.bbangle.wishlist.domain.QWishListStore;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardQueryDSLRepository {

    public static final int BOARD_PAGE_SIZE = 10;

    private static final QBoard board = QBoard.board;
    private static final QProduct product = QProduct.product;
    private static final QStore store = QStore.store;
    private static final QWishListBoard wishListBoard = QWishListBoard.wishListBoard;
    private static final QWishListFolder folder = QWishListFolder.wishListFolder;
    private static final QProductImg productImg = QProductImg.productImg;
    private static final QBoardDetail boardDetail = QBoardDetail.boardDetail;
    private static final QWishListStore wishlistStore = QWishListStore.wishListStore;
    private static final QRanking ranking = QRanking.ranking;

    private final BoardQueryProviderResolver boardQueryProviderResolver;
    private final JPAQueryFactory queryFactory;

    @Override
    public BoardCustomPage<List<BoardResponseDto>> getBoardResponseList(
        FilterRequest filterRequest,
        SortType sort,
        CursorInfo cursorInfo
    ) {
        BooleanBuilder filter = new BoardFilterCreator(filterRequest).create();

        List<Board> boards = boardQueryProviderResolver.resolve(sort, cursorInfo)
            .findBoards(filter);

        // FIXME: 요 아래부분은 service 에서 해야되지않나 싶은 부분... 레파지토리의 역할은 board 리스트 넘겨주는곳 까지가 아닐까 싶어서요
        List<BoardResponseDto> content = convertToBoardResponse(boards);
        return getBoardCustomPage(sort, cursorInfo, filter, content, isHasNext(boards));
    }

    @Override
    public List<BoardResponseDao> getAllByFolder(
        FolderBoardSortType sort,
        Long cursorId,
        WishListFolder folder,
        Long memberId
    ) {
        BooleanBuilder cursorBuilder = new BoardInFolderCursorGeneratorMapping(memberId, queryFactory, sort)
            .mappingCursorGenerator()
            .getCursor(cursorId);
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

    private List<BoardDetailDto> fetchBoardDetails(Long boardId) {
        return queryFactory.select(new QBoardDetailDto(
                boardDetail.id,
                boardDetail.imgIndex,
                boardDetail.url
            ))
            .from(boardDetail)
            .where(board.id.eq(boardId))
            .fetch();
    }

    private List<String> getTagsToStringList(Product product) {
        List<String> tags = new ArrayList<>();
        if (product.isGlutenFreeTag()) {
            tags.add(TagEnum.GLUTEN_FREE.label());
        }
        if (product.isSugarFreeTag()) {
            tags.add(TagEnum.SUGAR_FREE.label());
        }

        if (product.isHighProteinTag()) {
            tags.add(TagEnum.HIGH_PROTEIN.label());
        }
        if (product.isVeganTag()) {
            tags.add(TagEnum.VEGAN.label());
        }
        if (product.isKetogenicTag()) {
            tags.add(TagEnum.KETOGENIC.label());
        }

        return tags;
    }

    public List<ProductDto> fetchProductDtoByBoardId(Long boardId) {
        List<Product> products = queryFactory.selectFrom(product)
            .where(board.id.eq(boardId))
            .fetch();

        return products.stream()
            .map(product1 ->
                ProductDto.builder()
                    .id(product1.getId())
                    .title(product1.getTitle())
                    .tags(getTagsToStringList(product1))
                    .category(product1.getCategory())
                    .build())
            .toList();
    }

    public List<String> getProductDtosToDuplicatedTags(List<ProductDto> productDtos) {
        return productDtos.stream()
            .map(ProductDto::tags)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .distinct()
            .collect(Collectors.toList());
    }

    public boolean isBundleBoard(List<ProductDto> productDtos) {
        return productDtos.stream()
            .map(ProductDto::category)
            .distinct()
            .count() > 1;
    }

    private void setWishlistBoard(List<Expression<?>> columns) {
        columns.add(wishListBoard.id);
    }

    private void setWishlistStore(List<Expression<?>> columns) {
        columns.add(wishlistStore.id);
    }

    private Expression[] setColomns(Long memberId) {
        List<Expression<?>> columns = new ArrayList<>();
        columns.add(store.id);
        columns.add(store.name);
        columns.add(store.profile);
        columns.add(board.id);
        columns.add(board.profile);
        columns.add(board.title);
        columns.add(board.price);
        columns.add(board.monday);
        columns.add(board.tuesday);
        columns.add(board.wednesday);
        columns.add(board.thursday);
        columns.add(board.friday);
        columns.add(board.saturday);
        columns.add(board.sunday);
        columns.add(board.purchaseUrl);
        columns.add(productImg.id);
        columns.add(productImg.url);

        if (memberId != null && memberId > 0) {
            setWishlistBoard(columns);
        }

        if (memberId != null && memberId > 0) {
            setWishlistStore(columns);
        }

        return columns.toArray(new Expression[0]);
    }

    private JPAQuery<Tuple> getBoardDetailSelect(Long memberId) {
        return queryFactory.select(setColomns(memberId));
    }

    private void setWishlistJoin(JPAQuery<Tuple> jpaQuery, Long memberId) {
        jpaQuery.leftJoin(wishListBoard)
            .on(wishListBoard.boardId.eq(board.id),
                wishListBoard.memberId.eq(memberId))
            .leftJoin(wishlistStore)
            .on(wishlistStore.store.id.eq(store.id),
                wishlistStore.member.id.eq(memberId),
                wishlistStore.isDeleted.eq(false));
    }

    public List<Tuple> fetchStoreAndBoardAndImageTuple(Long memberId, Long boardId) {
        JPAQuery<Tuple> jpaQuery = getBoardDetailSelect(memberId).from(board)
            .where(board.id.eq(boardId))
            .join(board.store, store)
            .leftJoin(productImg)
            .on(board.id.eq(productImg.board.id));

        if (memberId != null && memberId > 0) {
            setWishlistJoin(jpaQuery, memberId);
        }

        return jpaQuery.fetch();
    }

    private List<BoardImgDto> getBoardImageToDto(List<Tuple> tuplesRelateBoard) {
        List<BoardImgDto> boardImgDtos = new ArrayList<>();
        for (Tuple tupleReleteBoard : tuplesRelateBoard) {
            boardImgDtos.add(
                BoardImgDto.builder()
                    .id(tupleReleteBoard.get(productImg.id))
                    .url(tupleReleteBoard.get(productImg.url))
                    .build()
            );
        }

        return boardImgDtos;
    }

    @Override
    public BoardDetailResponse getBoardDetailResponse(Long memberId, Long boardId) {
        List<Tuple> tuplesRelateBoard = fetchStoreAndBoardAndImageTuple(memberId, boardId);
        List<ProductDto> productDtos = fetchProductDtoByBoardId(boardId);
        List<String> duplicatedTags = getProductDtosToDuplicatedTags(productDtos);
        Boolean isBundled = isBundleBoard(productDtos);
        List<BoardDetailDto> boardDetails = fetchBoardDetails(boardId);

        Tuple tupleReleteBoard = tuplesRelateBoard.get(0);
        List<BoardImgDto> boardImgDtos = getBoardImageToDto(tuplesRelateBoard);

        StoreDto storeDto = StoreDto.builder()
            .storeId(tupleReleteBoard.get(store.id))
            .storeName(tupleReleteBoard.get(store.name))
            .profile(tupleReleteBoard.get(store.profile))
            .isWished(tupleReleteBoard.get(wishlistStore.id) != null)
            .build();

        BoardDetailSelectDto boardDto = BoardDetailSelectDto.builder()
            .boardId(tupleReleteBoard.get(board.id))
            .thumbnail(tupleReleteBoard.get(board.profile))
            .title(tupleReleteBoard.get(board.title))
            .price(tupleReleteBoard.get(board.price))
            .orderAvailableDays(
                BoardAvailableDayDto.builder()
                    .mon(tupleReleteBoard.get(board.monday))
                    .tue(tupleReleteBoard.get(board.tuesday))
                    .wed(tupleReleteBoard.get(board.wednesday))
                    .thu(tupleReleteBoard.get(board.thursday))
                    .fri(tupleReleteBoard.get(board.friday))
                    .sat(tupleReleteBoard.get(board.saturday))
                    .sun(tupleReleteBoard.get(board.sunday))
                    .build()
            )
            .purchaseUrl(tupleReleteBoard.get(board.purchaseUrl))
            .detail(boardDetails)
            .products(productDtos)
            .images(boardImgDtos.stream()
                .toList())
            .tags(duplicatedTags)
            .isWished(tupleReleteBoard.get(wishListBoard.id) != null)
            .isBundled(isBundled)
            .build();

        return BoardDetailResponse.builder()
            .store(storeDto)
            .board(boardDto)
            .build();
    }

    @Override
    public HashMap<Long, String> getAllBoardTitle() {
        List<Tuple> fetch = queryFactory
            .select(board.id, board.title)
            .from(board)
            .fetch();

        HashMap<Long, String> boardMap = new HashMap<>();
        fetch.forEach((tuple) -> boardMap.put(tuple.get(board.id), tuple.get(board.title)));

        return boardMap;
    }

    @Override
    public List<Board> checkingNullRanking() {
        return queryFactory.select(board)
            .from(board)
            .leftJoin(ranking)
            .on(board.eq(ranking.board))
            .where(ranking.id.isNull())
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

    private BoardCustomPage<List<BoardResponseDto>> getBoardCustomPage(
        SortType sort,
        CursorInfo cursorInfo,
        BooleanBuilder filter,
        List<BoardResponseDto> content,
        boolean hasNext
    ) {
        if (content.isEmpty()) {
            return BoardCustomPage.emptyPage();
        }

        Long boardCursor = content.get(content.size() - 1)
            .getBoardId();
        Double cursorScore = queryFactory
            .select(getScoreColumnBySortType(sort))
            .from(ranking)
            .join(board)
            .on(ranking.board.eq(board))
            .fetchJoin()
            .where(ranking.board.id.eq(boardCursor))
            .fetchFirst();

        if (Objects.isNull(cursorInfo) || Objects.isNull(cursorInfo.targetId())) {
            // FIXME: count 쿼리 분리 필요
            Long boardCnt = queryFactory
                .select(board.countDistinct())
                .from(store)
                .join(board)
                .on(board.store.eq(store))
                .join(product)
                .on(product.board.eq(board))
                .where(filter)
                .fetchOne();

            if (Objects.isNull(boardCnt)) {
                boardCnt = 0L;
            }

            Long storeCnt = queryFactory
                .select(store.countDistinct())
                .from(store)
                .join(board)
                .on(board.store.eq(store))
                .join(product)
                .on(product.board.eq(board))
                .where(filter)
                .fetchOne();

            if (Objects.isNull(storeCnt)) {
                storeCnt = 0L;
            }

            return BoardCustomPage.from(content, boardCursor, cursorScore, hasNext, boardCnt,
                storeCnt);
        }
        return BoardCustomPage.from(content, boardCursor, cursorScore, hasNext);
    }

    private NumberPath<Double> getScoreColumnBySortType(SortType sort) {
        return SortType.POPULAR.equals(sort) ? ranking.popularScore : ranking.recommendScore;
    }

    private boolean isHasNext(List<Board> boards) {
        return boards.size() >= BOARD_PAGE_SIZE + 1;
    }

    private List<BoardResponseDto> convertToBoardResponse(List<Board> boards) {
        Map<Long, List<String>> tagMapByBoardId = boards.stream()
            .collect(Collectors.toMap(
                Board::getId,
                board -> extractTags(board.getProductList())
            ));

        return boards.stream()
            .limit(BOARD_PAGE_SIZE)
            .map(board -> BoardResponseDto.from(board, tagMapByBoardId.get(board.getId())))
            .toList();
    }

    private List<String> extractTags(List<Product> products) {
        if (products == null) {
            return Collections.emptyList();
        }

        HashSet<String> tags = new HashSet<>();
        for (Product dto : products) {
            addTagIfTrue(tags, dto.isGlutenFreeTag(), TagEnum.GLUTEN_FREE.label());
            addTagIfTrue(tags, dto.isHighProteinTag(), TagEnum.HIGH_PROTEIN.label());
            addTagIfTrue(tags, dto.isSugarFreeTag(), TagEnum.SUGAR_FREE.label());
            addTagIfTrue(tags, dto.isVeganTag(), TagEnum.VEGAN.label());
            addTagIfTrue(tags, dto.isKetogenicTag(), TagEnum.KETOGENIC.label());
        }
        return new ArrayList<>(tags);
    }

    private void addTagIfTrue(Set<String> tags, boolean condition, String tag) {
        if (condition) {
            tags.add(tag);
        }
    }

}
