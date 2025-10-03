package com.bbangle.bbangle.seller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "계좌 정보 변경 요청 DTO")
public record SellerAccountUpdateRequest_v2(
    @Schema(description = "상점 ID", example = "1")
    @NotNull(message = "상점 ID는 필수입니다.")
    Long storeId,

    @Schema(description = "은행명", example = "국민은행")
    @NotBlank(message = "은행명은 필수입니다.")
    String bankName,

    @Schema(description = "예금주", example = "홍길동")
    @NotBlank(message = "예금주는 필수입니다.")
    String accountHolder,

    @Schema(description = "계좌번호", example = "123456-78-901234")
    @NotBlank(message = "계좌번호는 필수입니다.")
    String accountNumber
) {
}
