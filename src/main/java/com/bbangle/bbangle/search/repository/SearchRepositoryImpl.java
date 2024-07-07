package com.bbangle.bbangle.search.repository;

import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.repository.basic.cursor.BoardCursorGeneratorMapping;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.search.domain.QSearch;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.repository.basic.SearchFilterCreator;
import com.bbangle.bbangle.search.repository.basic.query.SearchQueryProviderResolver;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchQueryDSLRepository {

    private static final QSearch search = QSearch.search;
    private static final int ONEDAY = 24;

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
    public List<KeywordDto> getRecencyKeyword(Member member) {
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
        queryFactory.update(search)
            .set(search.isDeleted, true)
            .where(
                search.member.eq(member)
                    .and(search.keyword.eq(keyword))
            )
            .execute();
    }
}
