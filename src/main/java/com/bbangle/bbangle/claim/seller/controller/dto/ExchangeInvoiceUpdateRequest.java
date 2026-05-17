package com.bbangle.bbangle.claim.seller.controller.dto;

import com.bbangle.bbangle.order.domain.model.CourierCompany;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "교환 재배송 운송장 수정 요청 DTO")
public record ExchangeInvoiceUpdateRequest(

    @Schema(description = "수정할 택배사 코드", example = "CJ_LOGISTICS")
    @NotNull(message = "택배사 코드는 필수입니다.")
    CourierCompany courierCode,

    @Schema(description = "수정할 운송장 번호 (10~14자리 숫자)", example = "9876543210", maxLength = 100)
    @NotBlank(message = "운송장 번호는 필수입니다.")
    @Pattern(regexp = "^[0-9]{10,14}$", message = "운송장 번호는 10~14자리 숫자여야 합니다.")
    String trackingNumber,

    @Schema(description = "수정 사유", example = "오입력으로 인한 운송장 재등록", maxLength = 500)
    @Size(max = 500)
    String reason

) {

    @Schema(hidden = true)
    @AssertTrue(message = "유효한 택배사 코드를 입력해야 합니다. NONE은 허용되지 않습니다.")
    public boolean isCourierCodeValid() {
        return courierCode != null && courierCode != CourierCompany.NONE;
    }
}
