package com.bbangle.bbangle.board.repository.basic.cursor;

import static org.assertj.core.api.Assertions.*;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.repository.folder.cursor.BoardCursorGenerator;
import com.bbangle.bbangle.board.sort.SortType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

class BoardCursorGeneratorMappingTest extends AbstractIntegrationTest {

    @Autowired
    BoardCursorGeneratorMapping boardCursorGeneratorMapping;

    @ParameterizedTest
    @EnumSource(SortType.class)
    @DisplayName("정상적으로 sortType에 맞는 리스트를 반환한다")
    void returnBoardCursorGenerator(SortType sortType) {
        //given, when
        BoardCursorGenerator boardCursorGenerator = boardCursorGeneratorMapping.mappingCursorGenerator(sortType);

        //then
        switch (sortType){
            case HIGH_PRICE -> assertThat(boardCursorGenerator).isInstanceOf(HighPriceCursorGenerator.class);
            case LOW_PRICE -> assertThat(boardCursorGenerator).isInstanceOf(LowPriceCursorGenerator.class);
            case HIGHEST_RATED -> assertThat(boardCursorGenerator).isInstanceOf(HighRatedCursorGenerator.class);
            case RECENT -> assertThat(boardCursorGenerator).isInstanceOf(RecentCursorGenerator.class);
            case MOST_REVIEWED -> assertThat(boardCursorGenerator).isInstanceOf(MostReviewedCursorGenerator.class);
            case MOST_WISHED -> assertThat(boardCursorGenerator).isInstanceOf(MostWishedCursorGenerator.class);
            case RECOMMEND -> assertThat(boardCursorGenerator).isInstanceOf(RecommendCursorGenerator.class);
        }

    }
}
