package com.bbangle.bbangle.store.admin.controller.dto;

import com.bbangle.bbangle.store.domain.model.StoreNameRejectCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class AdminStoreRequest {

    @Schema(description = "판매자 스토어명 변경 요청 거절 사유 DTO")
    public record UpdateStoreNameRejectRequest(
        @Schema(description = "요청 거부 종류", example = "ETC")
        @NotNull
        StoreNameRejectCategory category,
        @Schema(description = "요청 거부 상세 사유", example = "부적절한 이름", nullable = true)
        String rejectDetail
    ) {}
}
