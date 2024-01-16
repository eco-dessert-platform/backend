package com.bbangle.bbangle.repository.impl;

import com.bbangle.bbangle.dto.*;
import com.bbangle.bbangle.model.QBoard;
import com.bbangle.bbangle.model.QProduct;
import com.bbangle.bbangle.model.QStore;
import com.bbangle.bbangle.model.TagEnum;
import com.bbangle.bbangle.repository.StoreQueryDSLRepository;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreQueryDSLRepository {
    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public StoreDetailResponseDto getStoreDetailResponseDto(Long storeId) {
        QStore store = QStore.store;
        QBoard board = QBoard.board;
        QProduct product = QProduct.product;

        List<Tuple> fetch = jpaQueryFactory.select(
                store.id,
                store.profile,
                store.name,
                store.introduce,
                board.id,
                board.profile,
                board.title,
                board.price,
                product.glutenFreeTag,
                product.highProteinTag,
                product.sugarFreeTag,
                product.veganTag,
                product.ketogenicTag,
//                product.category,
                board.view
        ).from(product)
                .join(product.board, board)
                .join(board.store, store)
                .where(board.store.id.eq(storeId))
                .orderBy(board.createdAt.desc())
                .fetch();

        StoreDto storeDto = StoreDto.builder().build();
        List<BoardDto> boardDtos = new ArrayList<>();

        // TagDto 초기화
        List<String> tags = new ArrayList<>();

        int resultSize = fetch.size();
        Long curProductId = fetch.get(0).get(board.id);
        int index = 0;

        for (Tuple tuple: fetch) {
            index++;

            // 개별 태그의 True를 각각 확인하여 전체 태그로 구성
            if (!tags.contains(TagEnum.GLUTEN_FREE.label()) && tuple.get(product.glutenFreeTag))
                tags.add(TagEnum.GLUTEN_FREE.label());

            if (!tags.contains(TagEnum.HIGH_PROTEIN.label()) && tuple.get(product.highProteinTag))
                tags.add(TagEnum.HIGH_PROTEIN.label());

            if (!tags.contains(TagEnum.SUGAR_FREE.label()) && tuple.get(product.sugarFreeTag))
                tags.add(TagEnum.SUGAR_FREE.label());

            if (!tags.contains(TagEnum.VEGAN.label()) && tuple.get(product.veganTag))
                tags.add(TagEnum.VEGAN.label());

            if (!tags.contains(TagEnum.KETOGENIC.label()) && tuple.get(product.ketogenicTag))
                tags.add(TagEnum.KETOGENIC.label());

            // ProductId가 달라지거나 반복문 마지막 일 시 Board 데이터 추가
            System.out.println(resultSize);
            System.out.println(index);
            if ( resultSize > index &&  tuple.get(board.id) != fetch.get(index).get(board.id) || resultSize == index){
                // 보드 리스트에 데이터 추가
                boardDtos.add(BoardDto.builder()
                        .id(tuple.get(board.id))
                        .profile(tuple.get(board.profile))
                        .title(tuple.get(board.title))
                        .price(tuple.get(board.price))
                        .isWished(true)
                        .isBundled(false)
                        .tags(tags)
                        .build());

                // 태그 초기화
                tags = new ArrayList<>();
            }


            // 반복문 마지막에 스토어 Dto 추가
            if (index == resultSize){
                storeDto = StoreDto.builder()
                        .id(tuple.get(store.id))
                        .profile(tuple.get(store.profile))
                        .name(tuple.get(store.name))
                        .introduce(tuple.get(store.introduce).isBlank() ? "": tuple.get(store.introduce))
                        .isWished(true)
                        .build();
            }
        }

        // 인기순을 찾기 위해 View를 기준으로 내림차순 정렬
        Collections.sort(fetch, (t1, t2) -> {
            // 내림차순으로 정렬하려는 값 선택 (여기서는 value1을 선택)
            return Integer.compare(t2.get(board.view), t1.get(board.view));
        });

        curProductId = -1L;

        List<BoardDto> bestBoardCollection = new ArrayList<>();
        for (Tuple tuple: fetch){
//            if curProductId !=
//
//            bestBoardCollection
        }

        List<BoardDto> bestBoards = fetch.stream()
                .map(
                        tuple -> BoardDto.builder()
                                .id(tuple.get(board.id))
                                .profile(tuple.get(board.profile))
                                .title(tuple.get(board.title))
                                .price(tuple.get(board.price))
                                .isBundled(false)
                                .build()
                )
                .distinct()
                .limit(3)
                .toList();

        return StoreDetailResponseDto.builder()
                .store(storeDto)
                .bestProducts(bestBoards)
                .allProducts(boardDtos)
                .build();
    }
}


