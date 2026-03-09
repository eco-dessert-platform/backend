package com.bbangle.bbangle.claim.seller.controller.dto;

import com.bbangle.bbangle.order.domain.model.CourierCompany;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "반품 운송장 입력 요청 DTO")
public record RegisterReturnInvoiceRequest(

    @Schema(description = "택배사 코드", example = "CJ_LOGISTICS")
    @NotNull(message = "택배사 코드는 필수입니다.")
    CourierCompany courierCode,

    @Schema(description = "운송장 번호 (10~14자리 숫자)", example = "1234567890")
    @NotBlank(message = "운송장 번호는 필수입니다.")
    @Pattern(regexp = "^[0-9]{10,14}$", message = "운송장 번호는 10~14자리 숫자여야 합니다.")
    String trackingNumber

) {
}