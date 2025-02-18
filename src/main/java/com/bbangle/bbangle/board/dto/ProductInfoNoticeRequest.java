package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.ProductInfoNotice;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ProductInfoNoticeRequest {
    private String productName;  // 제품명
    private String foodType;    // 식품의 유형
    private String manufacturer;    // 생산자
    private String originLocation;    // 소재지
    private String manufactureDate;  // 제조년월일
    private String expirationDate; // 소비기한 또는 품질 유지기한
    private String storageGuide;    // 포장 단위 별 내용물의 용량(중량) 수량
    private String packagingQuantityUnit; // 포장 단위별 수량
    private String rawMaterialName;  // 원재료명 (농수산물의 원산지 표시 등에 관한 법률)
    private String nutritionInfo;   // 영양성분
    private String transgenic;  // 유전자 변형 식품에 해당하는 경우의 표시
    private String customerWaring; // 소비자 안전을 위한 주의사항
    private String importFood;    // 수입 식품의 경우

    public ProductInfoNotice toEntity(Board board) {
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
                importFood,
                board
        );
    }
}
