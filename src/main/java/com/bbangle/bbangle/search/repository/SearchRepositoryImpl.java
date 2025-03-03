package com.bbangle.bbangle.search.repository;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic.boardStatistic;
import static com.bbangle.bbangle.search.domain.QSearch.search;
import static com.bbangle.bbangle.store.domain.QStore.store;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.basic.cursor.BoardCursorGeneratorMapping;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.dto.QKeywordDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchQueryDSLRepository {

    private static final Integer BOARD_PAGE_SIZE_PLUS_ONE = BOARD_PAGE_SIZE + 1;

    private final BoardCursorGeneratorMapping boardCursorGeneratorMapping;
    private final JPAQueryFactory queryFactory;


    @Override
    public List<Board> getBoardResponseList(
        String keyword,
        FilterRequest filterRequest,
        SortType sort,
        Long cursorId
    ) {
        BooleanBuilder filter = new SearchFilterCreator(keyword, filterRequest).create();
        BooleanBuilder cursorInfo = boardCursorGeneratorMapping.mappingCursorGenerator(sort)
            .getCursor(cursorId);
        OrderSpecifier<?>[] orderExpression = sort.getOrderExpression();

        return queryFactory.selectFrom(board)
            .join(board.store, store).fetchJoin()
            .leftJoin(board.products, product).fetchJoin()
            .leftJoin(board.boardStatistic, boardStatistic).fetchJoin()
            .where(
                cursorInfo,
                filter)
            .orderBy(orderExpression)
            .limit(BOARD_PAGE_SIZE_PLUS_ONE)
            .fetch();
    }

    @Override
    public Long getAllCount(
        String keyword,
        FilterRequest filterRequest
    ) {
        BooleanBuilder filter = new SearchFilterCreator(keyword, filterRequest).create();

        return queryFactory.select(board.id)
            .distinct()
            .from(product)
            .join(board)
            .on(product.board.id.eq(board.id))
            .join(board.boardStatistic, boardStatistic)
            .where(filter)
            .fetch().stream().count();
    }

    @Override
    public List<KeywordDto> getRecencyKeyword(Long memberId) {
        return queryFactory.select(
                new QKeywordDto(
                    search.keyword))
            .distinct()
            .from(search)
            .where(
                search.isDeleted.eq(false),
                search.member.id.eq(memberId))
            .orderBy(search.id.desc())
            .limit(7)
            .fetch();
    }

    @Override
    public String[] getBestKeyword(
        LocalDateTime beforeOneDayTime
    ) {
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
    public void markAsDeleted(String keyword, Long memberId) {
        queryFactory.update(search)
            .set(search.isDeleted, true)
            .where(
                search.member.id.eq(memberId)
                    .and(search.keyword.eq(keyword))
            )
            .execute();
    }

}
