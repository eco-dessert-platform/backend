package com.bbangle.bbangle.repository.impl;

import com.bbangle.bbangle.dto.*;
import com.bbangle.bbangle.model.*;
import com.bbangle.bbangle.repository.SearchQueryDSLRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class SearchQueryDSLRepositoryImpl implements SearchQueryDSLRepository {
    private final JPAQueryFactory queryFactory;
    private final int ONEDAY = 24;

    @Override
    public Slice<BoardResponseDto> getSearchResult(List<Long> boardIdes, Pageable pageable) {

        QBoard board = QBoard.board;
        QProduct product = QProduct.product;
        QStore store = QStore.store;

        List<Board> boards = queryFactory
                .selectFrom(board)
                .leftJoin(board.productList, product).fetchJoin() // Product와의 연관 관계를 fetch join으로 가져옴
                .leftJoin(board.store, store).fetchJoin()
                .where(board.id.in(boardIdes))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        Map<Long, List<ProductTagDto>> productTagsByBoardId = new HashMap<>();
        for (Board board1 : boards) {
            for (Product product1 : board1.getProductList()) {
                productTagsByBoardId.put(board1.getId(),
                        productTagsByBoardId.getOrDefault(board1.getId(), new ArrayList<>()));
                productTagsByBoardId.get(board1.getId()).add(ProductTagDto.from(product1));
            }
        }
//
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


        //   Slice 객체 반환
        return new SliceImpl<>(content, pageable, hasNext);
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
    public List<KeywordDto> getRecencyKeyword(Member member) {
        QSearch search = QSearch.search;

        return queryFactory.select(
                new QKeywordDto(search.id, search.keyword))
                .from(search)
                .where(search.member.eq(member), search.isDeleted.eq(false))
                .orderBy(search.createdAt.desc())
                .groupBy(search.keyword)
                .limit(7)
                .fetch();
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
}
