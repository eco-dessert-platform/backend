package com.bbangle.bbangle.seller.seller.service.info;

import com.bbangle.bbangle.seller.domain.AccountVerification;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "계좌 인증 응답")
public record AccountVerificationInfo(
    @Schema(description = "계좌인증 ID", example = "1")
    Long id,

    @Schema(description = "판매자 ID", example = "1")
    Long sellerId,

    @Schema(description = "계좌인증 성공 여부", example = "true")
    boolean verified,

    @Schema(description = "생성 일시", example = "2025-12-10T14:32:10")
    LocalDateTime createdAt
) {

    /**
     * Create an AccountVerificationInfo DTO from an AccountVerification domain object.
     *
     * @param accountVerification the domain object to convert
     * @return an AccountVerificationInfo containing id, sellerId, verified, and createdAt from the given accountVerification
     */
    public static AccountVerificationInfo from(AccountVerification accountVerification) {
        return new AccountVerificationInfo(
            accountVerification.getId(),
            accountVerification.getSeller().getId(),
            accountVerification.isVerified(),
            accountVerification.getCreatedAt()
        );
    }
}