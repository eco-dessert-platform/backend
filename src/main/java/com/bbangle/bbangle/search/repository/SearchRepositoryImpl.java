package com.bbangle.bbangle.search.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.search.dto.KeywordDto;
import com.bbangle.bbangle.search.dto.QKeywordDto;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.search.service.dto.QSearchInfo_CursorCondition;
import com.bbangle.bbangle.search.service.dto.SearchCommand;
import com.bbangle.bbangle.search.service.dto.SearchInfo;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.board.repository.BoardRepositoryImpl.BOARD_PAGE_SIZE;
import static com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic.boardStatistic;
import static com.bbangle.bbangle.search.domain.QSearch.search;
import static com.bbangle.bbangle.store.domain.QStore.store;

@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchQueryDSLRepository {

        private static final Integer BOARD_PAGE_SIZE_PLUS_ONE = BOARD_PAGE_SIZE + 1;

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
        public List<Board> getBoardResponseList(SearchCommand.Main command, SearchInfo.CursorCondition condition) {
                return queryFactory.selectFrom(board)
                    .join(board.store, store).fetchJoin()
                    .leftJoin(board.boardStatistic, boardStatistic).fetchJoin()
                    .where(
                        searchFilter.getLikeKeyword(command.keyword()),
                        searchFilter.getEqualTag(command.filterRequest()),
                        searchFilter.getBetweenPrice(command.filterRequest()),
                        searchFilter.getCategory(command.filterRequest()),
                        searchFilter.getCursorCondition(command.sort(), condition),
                        board.isDeleted.isFalse()
                    )
                    .orderBy(searchSort.getSortType(command.sort()))
                    .limit(BOARD_PAGE_SIZE_PLUS_ONE)
                    .fetch();
        }

        @Override
        public Long getAllCount(
            SearchCommand.Main command,
            SearchInfo.CursorCondition condition
        ) {
                return queryFactory.select(board.count())
                    .from(board)
                    .where(
                        searchFilter.getLikeKeyword(command.keyword()),
                        searchFilter.getEqualTag(command.filterRequest()),
                        searchFilter.getBetweenPrice(command.filterRequest()),
                        searchFilter.getCategory(command.filterRequest()),
                        searchFilter.getCursorCondition(command.sort(), condition),
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
