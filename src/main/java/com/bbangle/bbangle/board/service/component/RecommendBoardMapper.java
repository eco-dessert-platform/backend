package com.bbangle.bbangle.board.service.component;

import com.bbangle.bbangle.board.dto.RecommendBoardCsvDto;
import com.bbangle.bbangle.board.domain.RecommendationSimilarBoard;
import com.bbangle.bbangle.board.domain.SimilarityTypeEnum;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RecommendBoardMapper {
    private static final String QUERY_ITEM = "query_item";
    private static final String RECOMMENDATION_ITEM = "recommendation_item";
    private static final String SCORE = "score";
    private static final String RANK = "rank";
    private static final String RECOMMENDATION_THEME = "recommendation_theme";
    private static final String MODEL_VERSION = "model_version";

    public List<RecommendBoardCsvDto> mapToCsvDto(Map<String, Integer> header, List<List<String>> csvData) {
        return csvData.stream().map(row -> RecommendBoardCsvDto.builder()
            .queryItem(Long.parseLong(row.get(header.get(QUERY_ITEM))))
            .recommendationItem(Long.parseLong(row.get(header.get(RECOMMENDATION_ITEM))))
            .score(BigDecimal.valueOf(Double.parseDouble(row.get(header.get(SCORE)))))
            .rank(Integer.parseInt(row.get(header.get(RANK))))
            .recommendationTheme(SimilarityTypeEnum.valueOf(row.get(header.get(RECOMMENDATION_THEME))))
            .modelVersion(row.get(header.get(MODEL_VERSION)))
            .build()
        ).toList();
    }

    public List<RecommendationSimilarBoard> mapToEntity(List<RecommendBoardCsvDto> recommendBoardCsvDtoEntities) {
        return recommendBoardCsvDtoEntities.stream()
            .map(entity ->
                    RecommendationSimilarBoard.create(
                        entity.getQueryItem(),
                        entity.getRank(),
                        entity.getRecommendationItem(),
                        entity.getScore(),
                        entity.getRecommendationTheme(),
                        entity.getModelVersion()))
            .toList();
    }
}
