package com.bbangle.bbangle.board.customer.dto;

import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;
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
    @Schema(description = "키토제닉 여부(현재 저지방으로 사용 중)", nullable = true, type = "boolean")
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
        validateMinPrice(minPrice);
        validateMaxPrice(maxPrice);
    }

    public static void validateMinPrice(Integer minPrice) {
        if (Objects.nonNull(minPrice) && minPrice < 0) {
            throw new BbangleException(BbangleErrorCode.PRICE_NOT_OVER_ZERO);
        }
    }

    public static void validateMaxPrice(Integer maxPrice) {
        if (Objects.nonNull(maxPrice) && maxPrice < 0) {
            throw new BbangleException(BbangleErrorCode.PRICE_NOT_OVER_ZERO);
        }
    }

}
