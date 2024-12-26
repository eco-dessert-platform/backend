package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.SimilarityTypeEnum;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendBoardCsvDto {
    private Long queryItem;
    private Long recommendationItem;
    private BigDecimal score;
    private Integer rank;
    private SimilarityTypeEnum recommendationTheme;
    private String modelVersion;
}
