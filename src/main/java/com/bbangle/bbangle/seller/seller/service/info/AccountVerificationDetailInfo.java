package com.bbangle.bbangle.seller.seller.service.info;

import com.bbangle.bbangle.seller.domain.AccountVerification;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "계좌 인증 조회 응답")
public record AccountVerificationDetailInfo(
    @Schema(description = "계좌인증 ID", example = "1")
    Long id,

    @Schema(description = "판매자 ID", example = "1")
    Long sellerId,

    @Schema(description = "은행코드 (토스페이먼츠 표준)", example = "92")
    String bankCode,

    @Schema(description = "계좌번호 (복호화)", example = "123412341234")
    String accountNumber,

    @Schema(description = "예금주명", example = "홍길동")
    String accountHolder,

    @Schema(description = "계좌인증 성공 여부", example = "true")
    boolean verified,

    @Schema(description = "생성 일시", example = "2025-12-10T14:32:10")
    LocalDateTime createdAt
) {

    public static AccountVerificationDetailInfo of(AccountVerification accountVerification,
                                                   String decryptedAccountNumber) {
        return new AccountVerificationDetailInfo(
            accountVerification.getId(),
            accountVerification.getSeller().getId(),
            accountVerification.getBankCode(),
            decryptedAccountNumber,
            accountVerification.getAccountHolder(),
            accountVerification.isVerified(),
            accountVerification.getCreatedAt()
        );
    }
}
