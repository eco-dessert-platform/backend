package com.bbangle.bbangle.store.seller.controller.dto;

import com.bbangle.bbangle.store.domain.StoreStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public class StoreResponse {

    @Schema(description = "스토어 상세 응답 DTO")
    public record StoreDetail(
        @Schema(description = "스토어 ID", example = "1") Long storeId,
        @Schema(description = "스토어 프로필 이미지 URL", example = "https://d37g3q9mfan3cw.cloudfront.net/store/000000/logo.png") String profile,
        @Schema(description = "스토어명", example = "빵그리의 오븐 즉석빵 상점") String storeName,
        @Schema(description = "스토어 소개", example = "건강한 디저트를 만드는 베이커리") String introduce,
        @Schema(description = "현재 로그인한 사용자가 위시리스트에 추가했는지 여부", example = "true") Boolean isWished
    ) {

    }

    @Schema(description = "스토어 검색 응답 DTO")
    public record SearchResponse(
        @Schema(description = "스토어 ID", example = "1") Long storeId,
        @Schema(description = "스토어명", example = "빵그리의 오븐 즉석빵 상점") String storeName
    ) {

    }

    @Schema(description = "판매자 스토어 중복 검사 응답 DTO")
    @Builder
    public record StoreNameCheck(
        @Schema(description = "스토어 이름 중복 여부 (false = 중복, true = 사용 가능)", example = "false") boolean available,
        @Schema(description = "스토어 상세 정보 (스토어가 존재할 경우)", nullable = true) SellerStoreDetail store
    ) {}

    @Schema(description = "판매자 스토어 등록 결과 응답 DTO")
    @Builder
    public record StoreRegisterResult(
        @Schema(description = "판매자 ID", example = "1") Long sellerId,
        @Schema(description = "스토어 상세 정보") SellerStoreDetail store
    ) {}

    @Schema(description = "판매자 스토어 상세 응답 DTO")
    public record SellerStoreDetail(
        @Schema(description = "스토어 ID", example = "1") Long storeId,
        @Schema(description = "스토어명", example = "빵그리의 오븐") String name,
        @Schema(description = "스토어 소개", example = "건강한 디저트를 만드는 베이커리") String introduce,
        @Schema(description = "스토어 프로필 이미지 URL", example = "https://d37g3q9mfan3cw.cloudfront.net/store/000000/logo.png") String profile,
        @Schema(description = "스토어 등록 상태", example = "NONE") StoreStatus status,
        @Schema(description = "스토어 연락처", example = "01012345678") String phoneNumber,
        @Schema(description = "스토어 추가 연락처", example = "01012345678") String subPhoneNumber,
        @Schema(description = "스토어 이메일", example = "user@example.com") String email,
        @Schema(description = "스토어 출고지 주소", example = "(우편번호) 성남시 금광동 222-31") String originAddress,
        @Schema(description = "스토어 출고지 상세 주소", example = "나동 202호") String originAddressDetail
    ) {}
}
