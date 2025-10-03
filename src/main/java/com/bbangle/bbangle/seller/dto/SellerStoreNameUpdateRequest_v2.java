package com.bbangle.bbangle.seller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "스토어명 변경 요청 DTO")
public record SellerStoreNameUpdateRequest_v2(
    @Schema(description = "상점 ID", example = "1")
    @NotNull(message = "상점 ID는 필수입니다.")
    Long storeId,

    @Schema(description = "변경할 스토어명", example = "빵그리의 새로운 오븐")
    @NotBlank(message = "스토어명은 필수입니다.")
    String storeName
) {
}
