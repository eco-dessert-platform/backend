package com.bbangle.bbangle.config.ranking;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record BoardGrade(
    Long boardId,
    int count,
    BigDecimal grade
) {

}
