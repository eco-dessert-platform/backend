package com.bbangle.bbangle.seller.admin.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

public class AdminSellerRequest {

    @Builder
    @Schema(description = "승인 또는 거절할 판매자의 스토어 등록 신청 목록 DTO")
    public record StoreApplicationIds(
        @NotEmpty(message = "신청 Id 목록은 비어 있을 수 없습니다.")
        @Schema(description = "스토어 신청서 Id 목록", example = "[1]")
        List<@NotNull Long> applicationIds
    ) {}
}
