package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.validator.BoardValidator;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "filter에 필요한 파라미터")
public record FilterRequest(
    @Schema(description = "글루틴이 없는지 여부", nullable = true, type = "boolean")
    Boolean glutenFreeTag,
    @Schema(description = "고단백 여부", nullable = true, type = "boolean")
    Boolean highProteinTag,
    @Schema(description = "무설탕 여부", nullable = true, type = "boolean")
    Boolean sugarFreeTag,
    @Schema(description = "비건 여부", nullable = true, type = "boolean")
    Boolean veganTag,
    @Schema(description = "키토제닉 여부", nullable = true, type = "boolean")
    Boolean ketogenicTag,
    @Parameter
    Category category,
    @Schema(description = "최저 가격 설정", nullable = true, type = "integer")
    Integer minPrice,
    @Schema(description = "최고 가격 설정", nullable = true, type = "integer")
    Integer maxPrice,
    @Schema(description = "금일 주문 가능 여부", nullable = true, type = "boolean")
    Boolean orderAvailableToday
) {

    public FilterRequest {
        BoardValidator.validateMinPrice(minPrice);
        BoardValidator.validateMaxPrice(maxPrice);
    }

}
