package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.board.domain.TagEnum;
import com.bbangle.bbangle.board.dto.StoreAllBoardDto;
import com.bbangle.bbangle.board.dto.StoreBestBoardDto;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.page.StoreCustomPage;
import com.bbangle.bbangle.store.domain.QStore;
import com.bbangle.bbangle.store.dto.QStoreResponseDto;
import com.bbangle.bbangle.store.dto.StoreDetailResponseDto;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.bbangle.bbangle.wishlist.domain.QWishListStore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.*;

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

    @Override
    public StoreDetailResponseDto getStoreDetailResponseDtoWithLike(Long memberId, Long storeId) {
        List<Tuple> fetch = queryFactory.select(
                store.id,
                store.profile,
                store.name,
                store.introduce,
                wishListStore.id,
                board.id,
                board.profile,
                board.title,
                board.price,
                board.view
            )
            .from(board)
            .where(board.store.id.eq(storeId))
            .join(board.store, store)
            .leftJoin(wishListStore)
            .on(wishListStore.store.eq(store), wishListStore.member.id.eq(memberId),
                wishListStore.isDeleted.eq(false))
            .orderBy(board.view.desc())
            .limit(3)
            .fetch();

        List<Long> boardIds = fetch.stream()
            .map(tuple -> tuple.get(board.id))
            .toList();

        List<Tuple> productCategory = queryFactory.select(
                board.id,
                product.category)
            .from(product)
            .where(product.board.id.in(boardIds))
            .distinct()
            .fetch();

        Map<Long, Boolean> isBundledMap = new HashMap<>();
        productCategory.forEach(tuple -> {
            Long boardId = tuple.get(board.id);

            if (isBundledMap.containsKey(boardId)) {
                // boardId가 이미 존재한다면, true로 업데이트
                isBundledMap.put(boardId, true);
            } else {
                // boardId가 존재하지 않는다면, 처음으로 추가하므로 false로 설정
                isBundledMap.put(boardId, false);
            }
        });

        var storeDto = StoreDto.builder();
        List<StoreBestBoardDto> storeBestBoardDtos = new ArrayList<>();
        for (Tuple tuple : fetch) {
            Long boardId = tuple.get(board.id);
            storeBestBoardDtos.add(
                StoreBestBoardDto.builder()
                    .boardId(boardId)
                    .title(tuple.get(board.title))
                    .thumbnail(tuple.get(board.profile))
                    .isBundled(isBundledMap.getOrDefault(boardId, false))
                    .price(tuple.get(board.price))
                    .build()
            );

            storeDto.storeName(tuple.get(store.name))
                .profile(tuple.get(store.profile))
                .introduce(tuple.get(store.introduce))
                .storeId(tuple.get(store.id))
                .isWished(tuple.get(wishListStore.id) != null ? true : false);
        }

        return StoreDetailResponseDto.builder()
            .store(storeDto.build())
            .bestProducts(storeBestBoardDtos)
            .build();
    }

    @Override
    public StoreDetailResponseDto getStoreDetailResponseDto(Long storeId) {
        List<Tuple> fetch = queryFactory.select(
                store.id,
                store.profile,
                store.name,
                store.introduce,
                board.id,
                board.profile,
                board.title,
                board.price,
                board.view
            )
            .from(board)
            .where(board.store.id.eq(storeId))
            .join(board.store, store)
            .orderBy(board.view.desc())
            .limit(3)
            .fetch();

        List<Long> boardIds = fetch.stream()
            .map(tuple -> tuple.get(board.id))
            .toList();

        List<Tuple> productCategory = queryFactory.select(
                board.id,
                product.category)
            .from(product)
            .where(product.board.id.in(boardIds))
            .distinct()
            .fetch();

        Map<Long, Boolean> isBundledMap = new HashMap<>();
        productCategory.forEach(tuple -> {
            Long boardId = tuple.get(board.id);

            if (isBundledMap.containsKey(boardId)) {
                // boardId가 이미 존재한다면, true로 업데이트
                isBundledMap.put(boardId, true);
            } else {
                // boardId가 존재하지 않는다면, 처음으로 추가하므로 false로 설정
                isBundledMap.put(boardId, false);
            }
        });

        var storeDto = StoreDto.builder();
        List<StoreBestBoardDto> storeBestBoardDtos = new ArrayList<>();
        for (Tuple tuple : fetch) {
            Long boardId = tuple.get(board.id);
            storeBestBoardDtos.add(
                StoreBestBoardDto.builder()
                    .boardId(boardId)
                    .title(tuple.get(board.title))
                    .thumbnail(tuple.get(board.profile))
                    .isBundled(isBundledMap.getOrDefault(boardId, false))
                    .price(tuple.get(board.price))
                    .build()
            );

            storeDto.storeName(tuple.get(store.name))
                .profile(tuple.get(store.profile))
                .introduce(tuple.get(store.introduce))
                .storeId(tuple.get(store.id))
                .isWished(true);
        }

        return StoreDetailResponseDto.builder()
            .store(storeDto.build())
            .bestProducts(storeBestBoardDtos)
            .build();
    }

    @Override
    public SliceImpl<StoreAllBoardDto> getAllBoardWithLike(
        Pageable pageable,
        Long memberId,
        Long storeId
    ) {
        // 고유한 board.id를 선택하기 위한 서브쿼리 생성
        List<Long> boardSubQuery = queryFactory
            .select(board.id)
            .from(board)
            .where(board.store.id.eq(storeId))
            .orderBy(board.createdAt.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        // 메인 쿼리에서 서브쿼리를 사용하여 필요한 데이터를 조인하여 가져옴
        List<Tuple> fetch = queryFactory.select(
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
                product.category
            )
            .from(product)
            .join(product.board, board)
            .leftJoin(wishListBoard)
            .on(wishListBoard.board.eq(board), wishListBoard.memberId.eq(memberId),
                wishListBoard.isDeleted.eq(false))
            .where(board.id.in(boardSubQuery))
            .fetch();

        List<StoreAllBoardDto> content = new ArrayList<>();

        // TagDto 초기화
        Set<String> allTag = new HashSet<>();
        List<String> tags = new ArrayList<>();
        Set<Category> categories = new HashSet<>();

        int resultSize = fetch.size();
        int index = 0;

        for (Tuple tuple : fetch) {
            index++;

            // 개별 태그의 True를 각각 확인하여 전체 태그로 구성
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
            allTag.addAll(tags);
            tags.clear();

            // ProductId가 달라지거나 반복문 마지막 일 시 Board 데이터 추가
            if (resultSize > index && tuple.get(board.id) != fetch.get(index)
                .get(board.id) || resultSize == index) {
                // 보드 리스트에 데이터 추가
                content.add(
                    StoreAllBoardDto.builder()
                        .boardId(tuple.get(board.id))
                        .thumbnail(tuple.get(board.profile))
                        .title(tuple.get(board.title))
                        .price(tuple.get(board.price))
                        .isWished(tuple.get(wishListBoard.id) != null ? true : false)
                        .isBundled(categories.size() > 1)
                        .tags(allTag.stream()
                            .toList())
                        .view(tuple.get(board.view))
                        .build());
                categories.clear();
                allTag.clear();
            }
        }

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }


    @Override
    public SliceImpl<StoreAllBoardDto> getAllBoard(Pageable pageable, Long storeId) {
        // 고유한 board.id를 선택하기 위한 서브쿼리 생성
        List<Long> boardSubQuery = queryFactory
            .select(board.id)
            .from(board)
            .where(board.store.id.eq(storeId))
            .orderBy(board.createdAt.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)
            .fetch();

        // 메인 쿼리에서 서브쿼리를 사용하여 필요한 데이터를 조인하여 가져옴
        List<Tuple> fetch = queryFactory.select(
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
                product.category
            )
            .from(product)
            .join(product.board, board)
            .where(board.id.in(boardSubQuery))
            .fetch();

        List<StoreAllBoardDto> content = new ArrayList<>();

        // TagDto 초기화
        Set<String> allTag = new HashSet<>();
        List<String> tags = new ArrayList<>();
        Set<Category> categories = new HashSet<>();

        int resultSize = fetch.size();
        int index = 0;

        for (Tuple tuple : fetch) {
            index++;

            // 개별 태그의 True를 각각 확인하여 전체 태그로 구성
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
            allTag.addAll(tags);
            tags.clear();

            // ProductId가 달라지거나 반복문 마지막 일 시 Board 데이터 추가
            if (resultSize > index && tuple.get(board.id) != fetch.get(index)
                .get(board.id) || resultSize == index) {
                // 보드 리스트에 데이터 추가
                content.add(
                    StoreAllBoardDto.builder()
                        .boardId(tuple.get(board.id))
                        .thumbnail(tuple.get(board.profile))
                        .title(tuple.get(board.title))
                        .price(tuple.get(board.price))
                        .isWished(true)
                        .isBundled(categories.size() > 1)
                        .tags(allTag.stream()
                            .toList())
                        .view(tuple.get(board.view))
                        .build());
                categories.clear();
                allTag.clear();
            }
        }

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) {
            content.remove(content.size() - 1);
        }

        return new SliceImpl<StoreAllBoardDto>(content, pageable, hasNext);
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
        if (responseDtos.isEmpty()){
            return StoreCustomPage.from(responseDtos, EMPTY_PAGE_CURSOR, EMPTY_PAGE_HAS_NEXT);
        }

        boolean hasNext = checkingHasNext(responseDtos);
        if (hasNext) {
            responseDtos.remove(responseDtos.get(responseDtos.size() - 1));
        }
        Long nextCursor = responseDtos.get(responseDtos.size() -1).getStoreId();

        if(Objects.nonNull(memberId)){
            findNextCursorPageWithLogin(responseDtos, memberId);
        }

        return StoreCustomPage.from(responseDtos, nextCursor, hasNext);
    }

    public List<StoreResponseDto> findNextCursorPageWithLogin(
        List<StoreResponseDto> cursorPage,
        Long memberId
    ) {
        List<Long> pageIds = getContentsIds(cursorPage);

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOTFOUND_MEMBER));

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
        for(Long id: wishedIds){
            for(StoreResponseDto response : cursorPage){
                if (id.equals(response.getStoreId())){
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
        Long startId = checkingBoardExistence(cursorId);

        booleanBuilder.and(store.id.goe(startId));
        return booleanBuilder;
    }

    private Long checkingBoardExistence(Long cursorId) {
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


