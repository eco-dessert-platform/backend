package com.bbangle.bbangle.boardstatistic.ranking;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record BoardGrade(
    Long boardId,
    int count,
    BigDecimal grade
) {

}
