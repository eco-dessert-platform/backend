package com.bbangle.bbangle.board.repository.basic.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.sort.SortType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

class BoardQueryProviderResolverTest extends AbstractIntegrationTest {

    @Autowired
    BoardQueryProviderResolver boardQueryProviderResolver;

    @ParameterizedTest
    @EnumSource(SortType.class)
    @DisplayName("정상적으로 BoardQueryProvider를 제공한다")
    void returnBoardQueryProvider(SortType sortType) {
        //given, when
        BoardQueryProvider provider = boardQueryProviderResolver.resolve(sortType);

        //then
        switch (sortType) {
            case HIGH_PRICE -> assertThat(provider).isInstanceOf(HighPriceBoardQueryProviderResolver.class);
            case LOW_PRICE -> assertThat(provider).isInstanceOf(LowPriceBoardQueryProviderResolver.class);
            case HIGHEST_RATED -> assertThat(provider).isInstanceOf(HighRatedBoardQueryProviderResolver.class);
            case RECENT -> assertThat(provider).isInstanceOf(RecentBoardQueryProviderResolver.class);
            case MOST_REVIEWED -> assertThat(provider).isInstanceOf(MostReviewedBoardQueryProviderResolver.class);
            case MOST_WISHED -> assertThat(provider).isInstanceOf(MostWishedBoardQueryProviderResolver.class);
            case RECOMMEND -> assertThat(provider).isInstanceOf(RecommendBoardQueryProviderResolver.class);
        }

    }

}
