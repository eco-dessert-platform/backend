package com.bbangle.bbangle.search.repository.basic.query;

import com.bbangle.bbangle.board.sort.SortType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchQueryProviderResolver {

    private final SearchRecommendSearchQueryProviderResolver boardQueryProviderResolver;
    private final SearchLowPriceSearchQueryProviderResolver lowPriceBoardQueryProviderResolver;
    private final SearchHighPriceSearchQueryProviderResolver highPriceBoardQueryProviderResolver;
    private final SearchRecentSearchQueryProviderResolver recentBoardQueryProviderResolver;
    private final SearchHighRatedSearchQueryProviderResolver highRatedBoardQueryProviderResolver;
    private final SearchMostWishedSearchQueryProviderResolver mostWishedBoardQueryProviderResolver;
    private final SearchMostReviewedSearchQueryProviderResolver mostReviewedBoardQueryProviderResolver;


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
