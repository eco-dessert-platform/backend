package com.bbangle.bbangle.search.repository.basic.query;

import com.bbangle.bbangle.board.sort.SortType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchQueryProviderResolver {

    private final SearchRecommendBoardQueryProviderResolver boardQueryProviderResolver;
    private final SearchLowPriceBoardQueryProviderResolver lowPriceBoardQueryProviderResolver;
    private final SearchHighPriceBoardQueryProviderResolver highPriceBoardQueryProviderResolver;
    private final SearchRecentBoardQueryProviderResolver recentBoardQueryProviderResolver;
    private final SearchHighRatedBoardQueryProviderResolver highRatedBoardQueryProviderResolver;
    private final SearchMostWishedBoardQueryProviderResolver mostWishedBoardQueryProviderResolver;
    private final SearchMostReviewedBoardQueryProviderResolver mostReviewedBoardQueryProviderResolver;


    public SearchQueryProvider resolve(SortType sortType) {
        if (sortType == null) {
            return boardQueryProviderResolver;
        }

        if (sortType == SortType.LOW_PRICE) {
            return lowPriceBoardQueryProviderResolver;
        }

        if (sortType == SortType.HIGH_PRICE) {
            return highPriceBoardQueryProviderResolver;
        }

        if (sortType == SortType.RECENT) {
            return recentBoardQueryProviderResolver;
        }

        if (sortType == SortType.HIGHEST_RATED) {
            return highRatedBoardQueryProviderResolver;
        }

        if (sortType == SortType.MOST_WISHED) {
            return mostWishedBoardQueryProviderResolver;
        }

        if(sortType == SortType.MOST_REVIEWED) {
            return mostReviewedBoardQueryProviderResolver;
        }

        return boardQueryProviderResolver;
    }

}
