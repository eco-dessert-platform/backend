package com.bbangle.bbangle.board.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스토어 응답 DTO")
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

}
