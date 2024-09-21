package com.bbangle.bbangle.search.repository;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.basic.cursor.BoardCursorGeneratorMapping;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.search.domain.QSearch;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.dto.QKeywordDto;
import com.bbangle.bbangle.search.repository.basic.SearchFilterCreator;
import com.bbangle.bbangle.search.repository.basic.query.SearchQueryProviderResolver;
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

    private static final QSearch search = QSearch.search;

    private final BoardCursorGeneratorMapping boardCursorGeneratorMapping;

    private final SearchQueryProviderResolver searchQueryProviderResolver;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BoardResponseDao> getBoardResponseList(
        List<Long> boardIds,
        FilterRequest filterRequest,
        SortType sort,
        Long cursorId
    ) {
        BooleanBuilder filter = new SearchFilterCreator(filterRequest).create();
        BooleanBuilder cursorInfo = boardCursorGeneratorMapping
            .mappingCursorGenerator(sort)
            .getCursor(cursorId);
        OrderSpecifier<?>[] orderExpression = sort.getOrderExpression();

        return searchQueryProviderResolver.resolve(sort)
            .findBoards(boardIds, filter, cursorInfo, orderExpression);
    }

    @Override
    public Long getAllCount(
        List<Long> boardIds,
        FilterRequest filterRequest,
        SortType sort
    ) {
        BooleanBuilder filter = new SearchFilterCreator(filterRequest).create();

        return searchQueryProviderResolver.resolve(sort)
            .getCount(boardIds, filter);
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
