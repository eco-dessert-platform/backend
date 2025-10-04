package com.bbangle.bbangle.review.customer.dto;

import com.bbangle.bbangle.review.domain.Badge;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "리뷰 요청 DTO")
public record ReviewRequest(
        @NotNull
        @Size(min = 3, max = 3)
        @Schema(description = "리뷰에 부여할 뱃지 목록 (3개 선택)", example = "[\"GOOD\", \"SOFT\", \"SWEET\"]")
        List<Badge> badges,
        @NotNull
        @DecimalMin(value = "0")
        @DecimalMax(value = "5")
        @Schema(description = "평점 (0 이상 5 이하의 소수)", example = "4.5")
        BigDecimal rate,
        @Schema(description = "리뷰 내용 (선택 사항)", example = "만족스러웠어요.")
        String content,
        @NotNull
        @Schema(description = "리뷰 대상 게시글 ID", example = "10")
        Long boardId,
        @Schema(description = "리뷰에 첨부할 이미지 URL 목록", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
        List<String> urls
) {
}
