package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.domain.QProductImg;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.BoardAllTitleDto;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.CursorInfo;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.dto.QBoardAllTitleDto;
import com.bbangle.bbangle.board.repository.query.BoardQueryProviderResolver;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.ranking.domain.QRanking;
import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.bbangle.bbangle.wishlist.domain.QWishListFolder;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardQueryDSLRepository {

    public static final int BOARD_PAGE_SIZE = 10;
    private static final QBoard board = QBoard.board;
    private static final QProductImg productImage = QProductImg.productImg;
    private static final QProduct product = QProduct.product;
    private static final QStore store = QStore.store;
    private static final QWishListBoard wishListBoard = QWishListBoard.wishListBoard;
    private static final QWishListFolder folder = QWishListFolder.wishListFolder;
    private static final QRanking ranking = QRanking.ranking;

    private final BoardQueryProviderResolver boardQueryProviderResolver;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BoardAllTitleDto> findTitleByBoardAll() {
        return queryFactory.select(
                new QBoardAllTitleDto(board.id, board.title))
            .from(board)
            .fetch();
    }

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
    public Slice<BoardResponseDto> getAllByFolder(
        String sort, Pageable pageable, Long wishListFolderId,
        WishListFolder selectedFolder
    ) {
        OrderSpecifier<?> orderSpecifier = sortTypeFolder(sort);

        List<Board> boards = queryFactory
            .selectFrom(board)
            .leftJoin(board.productList, product)
            .fetchJoin()
            .leftJoin(board.store, store)
            .fetchJoin()
            .join(board)
            .on(board.id.eq(wishListBoard.boardId))
            .join(wishListBoard)
            .on(wishListBoard.wishlistFolderId.eq(folder.id))
            .where(wishListBoard.wishlistFolderId.eq(selectedFolder.getId()))
            .offset(pageable.getOffset())
            .orderBy(orderSpecifier)
            .limit(pageable.getPageSize() + 1)
            .fetch();

        boolean hasNext = boards.size() > pageable.getPageSize();
        List<BoardResponseDto> content = boards.stream()
            .limit(pageable.getPageSize())
            .map(board -> BoardResponseDto.inFolder(board, extractTags(board.getProductList())))
            .toList();

        return new SliceImpl<>(content, pageable, hasNext);
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

        Long boardCursor = content.get(content.size() - 1).getBoardId();
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

    private OrderSpecifier<?> sortTypeFolder(String sort) {
        OrderSpecifier<?> orderSpecifier;
        if (sort == null) {
            orderSpecifier = wishListBoard.createdAt.desc();
            return orderSpecifier;
        }
        orderSpecifier = switch (SortType.fromString(sort)) {
            case RECENT -> wishListBoard.createdAt.desc();
            case LOW_PRICE -> BoardRepositoryImpl.board.price.asc();
            case POPULAR -> BoardRepositoryImpl.board.wishCnt.desc();
            default -> throw new BbangleException("Invalid SortType");
        };
        return orderSpecifier;
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
