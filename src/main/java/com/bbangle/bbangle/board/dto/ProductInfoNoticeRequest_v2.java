package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.ProductInfoNotice;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 정보 고시 요청 DTO")
public record ProductInfoNoticeRequest_v2(
    @Schema(description = "제품명", example = "비건 쿠키 3종 모음")
    String productName,

    @Schema(description = "식품의 유형(기본값일 경우 게시글 상품 카테고리 자동 등록)", example = "쿠키, 빵")
    String foodType,

    @Schema(description = "제조사 또는 생산자(기본값일 경우 판매업체 자동 등록)", example = "빵그리의 오븐")
    String manufacturer,

    @Schema(description = "소재지(기본값일 경우 판매자 소재지 자동 등록)", example = "서울특별시 강남구 건강쿠키로 123")
    String originLocation,

    @Schema(description = "제조일자", example = "주문 후 익일 제조 (금, 주말 주문건은 월요일 제작)")
    String manufactureDate,

    @Schema(description = "소비기한 또는 품질 유지기한", example = "출고 후 바로 섭취", defaultValue = "출고 후 냉동 2주, 냉장 2일")
    String expirationDate,

    @Schema(description = "포장 단위 별 내용물의 용량(중량)", example = "30g", defaultValue = "상세페이지 첨부")
    String storageGuide,

    @Schema(description = "포장 단위 별 수량", example = "30개", defaultValue = "상세페이지 첨부")
    String packagingQuantityUnit,

    @Schema(description = "원재료명", example = "통밀가루30%, 카카오10%, ...", defaultValue = "상세페이지 첨부")
    String rawMaterialName,

    @Schema(description = "영양성분", defaultValue = "해당사항 없음")
    String nutritionInfo,

    @Schema(description = "유전자 변형 식품에 해당하는 경우의 표시", example = "해당사항 없음", defaultValue = "해당사항 없음")
    String transgenic,

    @Schema(description = "소비자 안전을 위한 주의사항", example = "배송 즉시 냉동보관 해주세요", defaultValue = "배송 즉시 냉동보관 해주세요")
    String customerWaring,

    @Schema(description = "수입 식품의 경우", example = "해당사항 없음", defaultValue = "해당사항 없음")
    String importFood
) {
    public ProductInfoNotice toEntity() {
        return new ProductInfoNotice(
            productName,
            foodType,
            manufacturer,
            originLocation,
            manufactureDate,
            expirationDate,
            storageGuide,
            packagingQuantityUnit,
            rawMaterialName,
            nutritionInfo,
            transgenic,
            customerWaring,
            importFood
        );
    }
}