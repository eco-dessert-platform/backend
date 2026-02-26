package com.bbangle.bbangle.store.seller.controller.dto;

import com.bbangle.bbangle.store.domain.model.StoreApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

public class StoreApplicationResponse {

    @Builder
    @Schema(description = "판매자 스토어 등록 신청 정보 DTO")
    public record StoreApplicationDetail(
        @Schema(description = "스토어 신청서 ID", example = "1") Long storeApplicationId,
        @Schema(description = "판매자 ID", example = "1") Long sellerId,
        @Schema(description = "스토어 ID", example = "1") Long storeId,
        @Schema(description = "스토어명", example = "빵그리의 오븐") String name,
        @Schema(description = "스토어 소개", example = "건강한 디저트를 만드는 베이커리") String introduce,
        @Schema(description = "스토어 프로필 이미지 URL", example = "https://d37g3q9mfan3cw.cloudfront.net/store/000000/logo.png") String profile,
        @Schema(description = "스토어 신청 상태", example = "PENDING") StoreApplicationStatus status,
        @Schema(description = "스토어 연락처", example = "01012345678") String phoneNumber,
        @Schema(description = "스토어 추가 연락처", example = "01012345678") String subPhoneNumber,
        @Schema(description = "스토어 이메일", example = "user@example.com") String email,
        @Schema(description = "스토어 출고지 주소", example = "(우편번호) 성남시 금광동 222-31") String originAddress,
        @Schema(description = "스토어 출고지 상세 주소", example = "나동 202호") String originAddressDetail,
        @Schema(description = "스토어 등록 신청일", example = "2026-01-01 12:00:00.123456") LocalDateTime createdAt,
        @Schema(description = "스토어 등록 수정일", example = "2026-01-01 12:00:00.123456") LocalDateTime modifiedAt
    ) {}
}
