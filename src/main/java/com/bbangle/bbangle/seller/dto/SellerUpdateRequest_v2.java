package com.bbangle.bbangle.seller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "판매자 정보 수정 요청 DTO")
public record SellerUpdateRequest_v2(
    @Schema(description = "상점 ID", example = "1")
    Long storeId,

    @Schema(description = "상점명", example = "빵그리의 오븐")
    String storeName,

    @Schema(description = "프로필 이미지 URL")
    String profile,

    @Schema(description = "한줄소개", example = "건강한 재료로 만드는 비건 베이커리")
    String shortDescription,

    @Schema(description = "대표 전화번호", example = "02-1234-5678")
    String phoneNumber,

    @Schema(description = "추가 연락처", example = "010-1234-5678")
    String subPhoneNumber,

    @Schema(description = "이메일 주소", example = "contact@bbangle.com")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$",
        message = "이메일 형식이 유효하지 않습니다"
    )
    String email,

    @Schema(description = "기본 주소", example = "성남시 금광동 2322-31")
    String originAddress,

    @Schema(description = "상세 주소", example = "나동 456호")
    String originAddressDetail
) {
}