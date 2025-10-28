package com.bbangle.bbangle.search.repository;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.domain.QProduct.product;
import static com.bbangle.bbangle.board.domain.QSegmentIntolerance.segmentIntolerance;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic.boardStatistic;
import static com.bbangle.bbangle.search.domain.QSearch.search;
import static com.bbangle.bbangle.store.domain.QStore.store;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.MemberSegment;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.search.customer.dto.KeywordDto;
import com.bbangle.bbangle.search.customer.dto.QKeywordDto;
import com.bbangle.bbangle.search.customer.service.dto.QSearchInfo_CursorCondition;
import com.bbangle.bbangle.search.customer.service.dto.SearchCommand;
import com.bbangle.bbangle.search.customer.service.dto.SearchInfo;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchQueryDSLRepository {

    private final JPAQueryFactory queryFactory;
    private final SearchFilter searchFilter;
    private final SearchSort searchSort;

    @Override
    public SearchInfo.CursorCondition getCursorCondition(Long cursorId) {
        return Optional.ofNullable(
                queryFactory.select(
                        new QSearchInfo_CursorCondition(
                            board.id,
                            boardStatistic.basicScore,
                            board.price,
                            boardStatistic.boardWishCount,
                            boardStatistic.boardReviewCount))
                    .from(boardStatistic)
                    .join(boardStatistic.board, board)
                    .where(board.id.eq(cursorId))
                    .fetchOne())
            .orElseThrow(() -> new BbangleException(
                BbangleErrorCode.RANKING_NOT_FOUND));
    }

    @Override
    public List<Board> getBoards(SearchCommand.Main command, SearchInfo.CursorCondition condition) {
        return queryFactory.selectFrom(board)
            .distinct()
            .join(board.store, store).fetchJoin()
            .leftJoin(board.boardStatistic, boardStatistic).fetchJoin()
            .leftJoin(board.products, product)
            .where(
                searchFilter.getLikeKeyword(command.keyword()),
                searchFilter.getEqualTag(command.filterRequest()),
                searchFilter.getBetweenPrice(command.filterRequest()),
                searchFilter.getCategory(command.filterRequest()),
                searchFilter.getDaysOfWeekCondition(command.filterRequest()),
                searchFilter.getCursorCondition(command.sort(), condition),
                board.isDeleted.isFalse()
            )
            .orderBy(searchSort.getSortType(command.sort()))
            .limit(command.limitSize() + 1)
            .fetch();
    }

    @Override
    public Long getAllCount(
        SearchCommand.Main command,
        SearchInfo.CursorCondition condition
    ) {
        return queryFactory.select(board.id.countDistinct())
            .from(board)
            .leftJoin(board.products, product)
            .where(
                searchFilter.getLikeKeyword(command.keyword()),
                searchFilter.getEqualTag(command.filterRequest()),
                searchFilter.getBetweenPrice(command.filterRequest()),
                searchFilter.getCategory(command.filterRequest()),
                board.isDeleted.isFalse()
            ).fetchOne();
    }

    @Override
    public List<Board> getRecommendBoardList(SearchCommand.Main command,
        SearchInfo.CursorCondition condition, MemberSegment memberSegment) {
        return queryFactory.selectFrom(board)
            .distinct()
            .join(board.store, store).fetchJoin()
            .leftJoin(board.boardStatistic, boardStatistic).fetchJoin()
            .leftJoin(board.products, product)
            .leftJoin(product.segmentIntolerances, segmentIntolerance)
            .where(
                searchFilter.getLikeKeyword(command.keyword()),
                searchFilter.getEqualTag(command.filterRequest()),
                searchFilter.getBetweenPrice(command.filterRequest()),
                searchFilter.getCategory(command.filterRequest()),
                searchFilter.getDaysOfWeekCondition(command.filterRequest()),
                searchFilter.getCursorCondition(command.sort(), condition),
                searchFilter.getExclusionCondition(memberSegment),
                segmentIntolerance.segment.eq(memberSegment.getSegment()),
                board.isDeleted.isFalse()
            )
            .orderBy(boardStatistic.boardWishCount.desc())
            .limit(command.limitSize() + 1)
            .fetch();
    }

    @Override
    public Long getRecommendAllCount(
        SearchCommand.Main command,
        SearchInfo.CursorCondition condition,
        MemberSegment memberSegment
    ) {
        return queryFactory.select(board.id.countDistinct())
            .from(board)
            .leftJoin(board.products, product)
            .leftJoin(product.segmentIntolerances, segmentIntolerance)
            .where(
                searchFilter.getLikeKeyword(command.keyword()),
                searchFilter.getEqualTag(command.filterRequest()),
                searchFilter.getBetweenPrice(command.filterRequest()),
                searchFilter.getCategory(command.filterRequest()),
                searchFilter.getExclusionCondition(memberSegment),
                segmentIntolerance.segment.eq(memberSegment.getSegment()),
                board.isDeleted.isFalse()
            ).fetchOne();
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
