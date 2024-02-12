package com.bbangle.bbangle.repository.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.bbangle.bbangle.dto.BoardAvailableDayDto;
import com.bbangle.bbangle.dto.BoardDetailResponseDto;
import com.bbangle.bbangle.dto.BoardDto;
import com.bbangle.bbangle.dto.BoardImgDto;
import com.bbangle.bbangle.dto.BoardResponseDto;
import com.bbangle.bbangle.dto.ProductDto;
import com.bbangle.bbangle.dto.ProductTagDto;
import com.bbangle.bbangle.dto.StoreDto;
import com.bbangle.bbangle.exception.CategoryTypeException;
import com.bbangle.bbangle.model.Board;
import com.bbangle.bbangle.model.Category;
import com.bbangle.bbangle.model.Product;
import com.bbangle.bbangle.model.QBoard;
import com.bbangle.bbangle.model.QProduct;
import com.bbangle.bbangle.model.QProductImg;
import com.bbangle.bbangle.model.QStore;
import com.bbangle.bbangle.model.QWishlistFolder;
import com.bbangle.bbangle.model.QWishlistProduct;
import com.bbangle.bbangle.model.TagEnum;
import com.bbangle.bbangle.repository.BoardQueryDSLRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<BoardResponseDto> getBoardResponseDto(String sort, Boolean glutenFreeTag, Boolean highProteinTag,
                                                       Boolean sugarFreeTag, Boolean veganTag, Boolean ketogenicTag,
                                                       String category, Integer minPrice, Integer maxPrice,
                                                       Pageable pageable) {

        QBoard board = QBoard.board;
        QProduct product = QProduct.product;
        QStore store = QStore.store;

        BooleanBuilder filter =
            setFilteringCondition(glutenFreeTag,
                highProteinTag,
                sugarFreeTag,
                veganTag,
                ketogenicTag,
                category,
                minPrice,
                maxPrice,
                product,
                board);


// Step 2: 메인 쿼리
        List<Board> boards = queryFactory
            .selectFrom(board)
            .leftJoin(board.productList, product).fetchJoin() // Product와의 연관 관계를 fetch join으로 가져옴
            .leftJoin(board.store, store).fetchJoin()
            .where(filter)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        Map<Long, List<ProductTagDto>> productTagsByBoardId = getLongListMap(boards);

        List<BoardResponseDto> content = new ArrayList<>();

        for (Board board1 : boards) {
            // 결과를 DTO로 변환
            content.add(BoardResponseDto.builder()
                .boardId(board1.getId())
                .storeId(board1.getStore().getId())
                .storeName(board1.getStore().getName())
                .thumbnail(board1.getProfile())
                .title(board1.getTitle())
                .price(board1.getPrice())
                .isWished(true) // 이 값은 필요에 따라 설정
                .tags(addList(productTagsByBoardId.get(board1.getId())))
                .build());

        }

        // 다음 페이지 존재 여부 확인
        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            // 마지막 항목 제거
            content.remove(content.size() - 1);
        }

        // Slice 객체 반환
        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public Slice<BoardResponseDto> getAllByFolder(String sort, Pageable pageable, Long wishListFolderId, List<Long> boardIds) {
        QBoard board = QBoard.board;
        QProduct product = QProduct.product;
        QStore store = QStore.store;

        List<Board> boards = queryFactory
            .selectFrom(board)
            .leftJoin(board.productList, product).fetchJoin()
            .leftJoin(board.store, store).fetchJoin()
            .where(board.id.in(boardIds))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        Map<Long, List<ProductTagDto>> productTagsByBoardId = getLongListMap(boards);

        List<BoardResponseDto> content = new ArrayList<>();

        for (Board board1 : boards) {
            content.add(BoardResponseDto.builder()
                .boardId(board1.getId())
                .storeId(board1.getStore().getId())
                .storeName(board1.getStore().getName())
                .thumbnail(board1.getProfile())
                .title(board1.getTitle())
                .price(board1.getPrice())
                .isWished(true)
                .tags(addList(productTagsByBoardId.get(board1.getId())))
                .build());

        }

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private static Map<Long, List<ProductTagDto>> getLongListMap(List<Board> boards) {
        Map<Long, List<ProductTagDto>> productTagsByBoardId = new HashMap<>();
        for (Board board1 : boards) {
            for (Product product1 : board1.getProductList()) {
                productTagsByBoardId.put(board1.getId(),
                    productTagsByBoardId.getOrDefault(board1.getId(), new ArrayList<>()));
                productTagsByBoardId.get(board1.getId()).add(ProductTagDto.from(product1));
            }
        }
        return productTagsByBoardId;
    }

    private static BooleanBuilder setFilteringCondition(Boolean glutenFreeTag, Boolean highProteinTag,
                                                        Boolean sugarFreeTag,
                                                        Boolean veganTag, Boolean ketogenicTag, String category,
                                                        Integer minPrice, Integer maxPrice,
                                                        QProduct product, QBoard board) {
        BooleanBuilder filterBuilder = new BooleanBuilder();
        if (glutenFreeTag != null) {
            filterBuilder.and(product.glutenFreeTag.eq(glutenFreeTag));
        }
        if (highProteinTag != null) {
            filterBuilder.and(product.highProteinTag.eq(highProteinTag));
        }
        if (sugarFreeTag != null) {
            filterBuilder.and(product.sugarFreeTag.eq(sugarFreeTag));
        }
        if (veganTag != null) {
            filterBuilder.and(product.veganTag.eq(veganTag));
        }
        if (ketogenicTag != null) {
            filterBuilder.and(product.ketogenicTag.eq(ketogenicTag));
        }
        if (category != null && !category.isBlank()) {
            if (!Category.checkCategory(category)) {
                throw new CategoryTypeException();
            }
            filterBuilder.and(product.category.eq(Category.valueOf(category)));
        }
        if (minPrice != null) {
            filterBuilder.and(board.price.goe(minPrice));
        }
        if (maxPrice != null) {
            filterBuilder.and(board.price.loe(maxPrice));
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

    @Override
    public BoardDetailResponseDto getBoardDetailResponseDto(Long boardId) {
        QBoard board = QBoard.board;
        QProduct product = QProduct.product;
        QStore store = QStore.store;
        QProductImg productImg = QProductImg.productImg;

        List<Tuple> fetch = queryFactory
            .select(
                store.id,
                store.name,
                store.profile,
                board.id,
                board.profile,
                board.title,
                board.price,
                productImg.id,
                productImg.url,
                board.title,
                board.price,
                board.monday,
                board.tuesday,
                board.wednesday,
                board.thursday,
                board.friday,
                board.saturday,
                board.sunday,
                board.purchaseUrl,
                board.detail,
                product.id,
                product.title,
                product.glutenFreeTag,
                product.highProteinTag,
                product.sugarFreeTag,
                product.veganTag,
                product.ketogenicTag)
            .from(product)
            .join(product.board, board)
            .join(board.store, store)
            .leftJoin(productImg).on(board.id.eq(productImg.board.id))
            .where(board.id.eq(boardId))
            .fetch();

        StoreDto storeDto = StoreDto.builder().build();
        BoardDto boardDto = BoardDto.builder().build();
        List<ProductDto> productDtos = new ArrayList<>();
        List<BoardImgDto> boardImgDtos = new ArrayList<>();

        int resultSize = fetch.size();
        int index = 0;

        List<Long> alreadyProductImgId = new ArrayList<>();
        List<Long> alreadyProductId = new ArrayList<>();

        for (Tuple tuple : fetch) {
            index++;

            List<String> tags = new ArrayList<>();
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

            // 중복 제거 및 상품 추가
            if (alreadyProductId.indexOf(tuple.get(product.id)) == -1) {
                productDtos.add(
                    ProductDto.builder()
                        .title(tuple.get(product.title))
                        .tags(tags).build());

                alreadyProductId.add(tuple.get(product.id));
            }

            // 중복 제거 및 보드 이미지 추가
            if (alreadyProductImgId.indexOf(tuple.get(productImg.id)) == -1 && tuple.get(productImg.id) != null) {
                boardImgDtos.add(
                    BoardImgDto.builder()
                        .id(tuple.get(productImg.id))
                        .url(tuple.get(productImg.url))
                        .build()
                );

                alreadyProductImgId.add(tuple.get(productImg.id));
            }

            // 반복문 마지막 때 보드 및 스토어 추가
            if (index == resultSize) {
                boardDto = BoardDto.builder()
                    .boardId(tuple.get(board.id))
                    .thumbnail(tuple.get(board.profile))
                    .images(boardImgDtos)
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
                    .isWished(true)
                    .isBundled(false)
                    .detail(tuple.get(board.detail))
                    .products(productDtos)
                    .build();

                storeDto = StoreDto.builder()
                    .storeId(tuple.get(store.id))
                    .storeName(tuple.get(store.name))
                    .profile(tuple.get(store.profile))
                    .isWished(true)
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

}
