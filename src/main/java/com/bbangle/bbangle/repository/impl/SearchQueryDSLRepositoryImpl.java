package com.bbangle.bbangle.repository.impl;

import com.bbangle.bbangle.dto.*;
import com.bbangle.bbangle.model.*;
import com.bbangle.bbangle.repository.SearchQueryDSLRepository;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SearchQueryDSLRepositoryImpl implements SearchQueryDSLRepository {
    private final JPAQueryFactory queryFactory;
    private final int ONEDAY = 24;

    @Override
    public List<BoardResponseDto> getSearchResult(List<Long> boardIdes) {

        QBoard board = QBoard.board;
        QProduct product = QProduct.product;
        QStore store = QStore.store;

        List<Tuple> boards = queryFactory
                .select(
                        store.id,
                        store.name,
                        board.id,
                        board.profile,
                        board.title,
                        board.price,
                        product.glutenFreeTag,
                        product.highProteinTag,
                        product.sugarFreeTag,
                        product.veganTag,
                        product.ketogenicTag
                )
                .from(product)
                .join(product.board, board)
                .join(board.store, store)
                .where(product.board.id.in(boardIdes))
                .fetch();


        Map<Long, BoardResponseDto> boardMap = new HashMap<>();

        for (Tuple tuple:boards) {
            Long boardId =  tuple.get(board.id);
            if (!boardMap.containsKey(boardId)) {
                boardMap.put(boardId,
                        BoardResponseDto.builder()
                                .boardId(boardId)
                                .storeId(tuple.get(store.id))
                                .storeName(tuple.get(store.name))
                                .thumbnail(tuple.get(board.profile))
                                .title(tuple.get(board.title))
                                .price(tuple.get(board.price))
                                .tags(new ArrayList<>())
                                .isWished(true) // 이 값은 필요에 따라 설정
                                .build());
            }

            BoardResponseDto boardResponseDto = boardMap.get(tuple.get(board.id));

            if (tuple.get(product.glutenFreeTag)) {
                System.out.println(boardResponseDto.tags());
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

            boardMap.put(tuple.get(board.id), boardResponseDto);
        }

        return boardMap.entrySet().stream().map(
                longBoardResponseDtoEntry -> longBoardResponseDtoEntry.getValue()
        ).map(
                boardResponseDto -> removeDuplicatesFromDto(boardResponseDto)
        ).toList();
    }

    @Override
    public List<StoreResponseDto> getSearchedStore(List<Long> storeIndexList){
        QStore store = QStore.store;

        return queryFactory
                .select(
                        store.id,
                        store.name,
                        store.introduce,
                        store.profile
                )
                .from(store)
                .where(store.id.in(storeIndexList))
                .fetch().stream().map(
                        tuple -> StoreResponseDto.fromWithoutLogin(
                                Store.builder()
                                        .id(tuple.get(store.id))
                                        .name(tuple.get(store.name))
                                        .introduce(tuple.get(store.introduce))
                                        .profile(tuple.get(store.profile))
                                        .build())).toList();
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
}
