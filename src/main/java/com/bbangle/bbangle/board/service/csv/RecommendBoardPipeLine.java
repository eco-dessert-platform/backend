package com.bbangle.bbangle.board.service.csv;

import com.bbangle.bbangle.board.domain.RecommendationSimilarBoard;
import com.bbangle.bbangle.board.domain.SimilarityModelVerEnum;
import com.bbangle.bbangle.board.domain.SimilarityTypeEnum;
import com.bbangle.bbangle.common.domain.csv.CsvPipeLineImpl;
import java.math.BigDecimal;
import java.util.List;

public class RecommendBoardPipeLine implements RecommendDefaultCsvPipeLine<List<RecommendBoardCsvEntity>>,
    RecommendSaveCsvPipeLine<List<RecommendBoardCsvEntity>, List<RecommendationSimilarBoard>> {

    private static final String QUERY_ITEM = "query_item";
    private static final String RECOMMENDATION_ITEM = "recommendation_item";
    private static final String SCORE = "score";
    private static final String RANK = "rank";
    private static final String RECOMMENDATION_THEME = "recommendation_theme";
    private static final String MODEL_VERSION = "model_version";

    private CsvPipeLineImpl<RecommendBoardCsvEntity, RecommendationSimilarBoard> csvPipeLine;

    public RecommendBoardPipeLine(List<List<String>> csvData) {
        csvPipeLine = new CsvPipeLineImpl<>(csvData);
    }

    @Override
    public List<RecommendBoardCsvEntity> mapToCsvEntity() {
        return csvPipeLine.mapToCsvEntity(row -> RecommendBoardCsvEntity.builder()
            .queryItem(Long.parseLong(row.get(csvPipeLine.getIndex(QUERY_ITEM))))
            .recommendationItem(
                Long.parseLong(row.get(csvPipeLine.getIndex(RECOMMENDATION_ITEM))))
            .score(BigDecimal.valueOf(Double.parseDouble(row.get(csvPipeLine.getIndex(SCORE)))))
            .rank(Integer.parseInt(row.get(csvPipeLine.getIndex(RANK))))
            .recommendationTheme(
                SimilarityTypeEnum.valueOf(row.get(csvPipeLine.getIndex(RECOMMENDATION_THEME))))
            .modelVersion(
                SimilarityModelVerEnum.valueOf(row.get(csvPipeLine.getIndex(MODEL_VERSION))))
            .build());
    }

    @Override
    public List<RecommendationSimilarBoard> mapToEntity(
        List<RecommendBoardCsvEntity> recommendBoardCsvEntities) {
        return csvPipeLine.mapToClass(recommendBoardCsvEntities, RecommendBoardCsvEntity::toEntity);
    }
}
