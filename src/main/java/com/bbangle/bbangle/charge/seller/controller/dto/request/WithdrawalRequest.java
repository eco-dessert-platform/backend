package com.bbangle.bbangle.charge.seller.controller.dto.request;

import com.bbangle.bbangle.charge.seller.service.model.SellerChargeCommand.WithdrawalCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "충전금 출금 신청 요청 DTO")
public record WithdrawalRequest(

    @Schema(description = "출금 신청 금액 (0 초과 ~ 현재 잔액 이하)", example = "50000")
    @NotNull(message = "출금 신청 금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "출금 금액은 0보다 커야 합니다.")
    BigDecimal withdrawalAmount,

    @Schema(description = "은행명", example = "국민은행")
    @NotBlank(message = "은행명은 필수입니다.")
    @Size(max = 30, message = "은행명은 30자 이하여야 합니다.")
    String bankName,

    @Schema(description = "예금주", example = "홍길동")
    @NotBlank(message = "예금주는 필수입니다.")
    @Size(max = 30, message = "예금주명은 30자 이하여야 합니다.")
    String accountHolder,

    @Schema(description = "계좌번호 (숫자만, 최대 20자)", example = "123456789012")
    @NotBlank(message = "계좌번호는 필수입니다.")
    @Pattern(regexp = "\\d{1,20}", message = "계좌번호는 숫자만, 최대 20자여야 합니다.")
    String accountNumber
) {

    public WithdrawalCommand toCommand(Long sellerId, String transactionId) {
        return WithdrawalCommand.builder()
            .sellerId(sellerId)
            .transactionId(transactionId)
            .withdrawalAmount(withdrawalAmount)
            .bankName(bankName)
            .accountHolder(accountHolder)
            .accountNumber(accountNumber)
            .build();
    }
}
