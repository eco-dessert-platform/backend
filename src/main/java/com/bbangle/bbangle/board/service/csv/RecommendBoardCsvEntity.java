package com.bbangle.bbangle.board.service.csv;

import com.bbangle.bbangle.board.domain.RecommendationSimilarBoard;
import com.bbangle.bbangle.board.domain.SimilarityModelVerEnum;
import com.bbangle.bbangle.board.domain.SimilarityTypeEnum;
import com.bbangle.bbangle.common.domain.csv.CsvEntity;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendBoardCsvEntity implements CsvEntity<RecommendationSimilarBoard> {
    private Long queryItem;
    private Long recommendationItem;
    private BigDecimal score;
    private Integer rank;
    private SimilarityTypeEnum recommendationTheme;
    private SimilarityModelVerEnum modelVersion;

    @Override
    public RecommendationSimilarBoard toEntity() {
        return RecommendationSimilarBoard.builder()
            .queryItem(queryItem)
            .recommendationTheme(recommendationTheme)
            .score(score)
            .rank(rank)
            .recommendationItem(recommendationItem)
            .modelVersion(modelVersion)
            .build();
    }
}
