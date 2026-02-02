package com.bbangle.bbangle.auth.seller.facade.dto;

import com.bbangle.bbangle.seller.domain.model.CertificationStatus;

/**
 * Token 발급 DTO
 * @param refreshToken Refresh 토큰
 * @param accessToken Access 토큰
 * @param sellerId Seller Id
 * @param status Seller 계정 상태
 */
public record GenerateTokenDTO(
    String refreshToken,
    String accessToken,
    Long sellerId,
    CertificationStatus status
) {

    public static GenerateTokenDTO of(
        String refreshToken,
        String accessToken,
        Long sellerId,
        CertificationStatus status
    ) {
        return new GenerateTokenDTO(refreshToken, accessToken, sellerId, status);
    }
}
