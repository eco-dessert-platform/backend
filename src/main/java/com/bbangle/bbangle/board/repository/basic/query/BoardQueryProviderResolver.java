package com.bbangle.bbangle.board.repository.basic.query;

import com.bbangle.bbangle.board.sort.SortType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardQueryProviderResolver {

    private final RecommendBoardQueryProviderResolver boardQueryProviderResolver;
    private final LowPriceBoardQueryProviderResolver lowPriceBoardQueryProviderResolver;
    private final HighPriceBoardQueryProviderResolver highPriceBoardQueryProviderResolver;
    private final RecentBoardQueryProviderResolver recentBoardQueryProviderResolver;
    private final HighRatedBoardQueryProviderResolver highRatedBoardQueryProviderResolver;
    private final MostWishedBoardQueryProviderResolver mostWishedBoardQueryProviderResolver;
    private final MostReviewedBoardQueryProviderResolver mostReviewedBoardQueryProviderResolver;


    public BoardQueryProvider resolve(SortType sortType) {
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
