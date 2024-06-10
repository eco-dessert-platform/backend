package com.bbangle.bbangle.board.repository.basic.cursor;

import com.bbangle.bbangle.board.repository.folder.cursor.BoardCursorGenerator;
import com.bbangle.bbangle.board.repository.folder.cursor.BoardInFolderCursorGenerator;
import com.bbangle.bbangle.board.sort.SortType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardCursorGeneratorMapping {

    private final RecommendCursorGenerator recommendCursorGenerator;
    private final LowPriceCursorGenerator lowPriceCursorGenerator;
    private final HighPriceCursorGenerator highPriceCursorGenerator;
    private final RecentCursorGenerator recentCursorGenerator;
    private final HighRatedCursorGenerator highRatedCursorGenerator;
    private final MostWishedCursorGenerator mostWishedCursorGenerator;
    private final MostReviewedCursorGenerator mostReviewedCursorGenerator;


    public BoardCursorGenerator mappingCursorGenerator(SortType sortType) {
        if (sortType == null) {
            return recommendCursorGenerator;
        }

        if (sortType == SortType.LOW_PRICE) {
            return lowPriceCursorGenerator;
        }

        if (sortType == SortType.HIGH_PRICE) {
            return highPriceCursorGenerator;
        }

        if (sortType == SortType.RECENT) {
            return recentCursorGenerator;
        }

        if (sortType == SortType.HIGHEST_RATED) {
            return highRatedCursorGenerator;
        }

        if (sortType == SortType.MOST_WISHED) {
            return mostWishedCursorGenerator;
        }

        if(sortType == SortType.MOST_REVIEWED) {
            return mostReviewedCursorGenerator;
        }

        return recommendCursorGenerator;
    }

}
