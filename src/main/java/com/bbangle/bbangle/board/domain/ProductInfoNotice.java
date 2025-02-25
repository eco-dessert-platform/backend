package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductInfoNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;              // 제품명
    private String foodType;                 // 식품 유형
    private String manufacturer;             // 제조사 / 생산자
    private String originLocation;           // 원산지 / 제조소 소재지
    private String manufactureDate;          // 제조년월일
    private String expirationDate;           // 소비기한 또는 품질 유지기한
    private String storageGuide;             // 보관 방법 및 주의사항
    private String packagingQuantityUnit;    // 포장 단위별 용량(중량) 및 수량
    private String rawMaterialName;          // 원재료명 및 함량
    private String nutritionInfo;            // 영양 정보
    private String transgenic;               // 유전자 변형 식품 여부
    private String customerWarning;          // 소비자 안전 정보 및 주의사항
    private String importFood;               // 수입 식품 관련 정보


    public ProductInfoNotice(String productName, String foodType, String manufacturer, String originLocation,
                             String manufactureDate, String expirationDate, String storageGuide,
                             String packagingQuantityUnit, String rawMaterialName, String nutritionInfo,
                             String transgenic, String customerWarning, String importFood) {
        validate(productName);
        this.productName = productName;
        this.foodType = foodType;
        this.manufacturer = manufacturer;
        this.originLocation = originLocation;
        this.manufactureDate = manufactureDate;
        this.expirationDate = expirationDate;
        this.storageGuide = storageGuide;
        this.packagingQuantityUnit = packagingQuantityUnit;
        this.rawMaterialName = rawMaterialName;
        this.nutritionInfo = nutritionInfo;
        this.transgenic = transgenic;
        this.customerWarning = customerWarning;
        this.importFood = importFood;
    }

    private void validate(String productName) {
        if (productName == null || productName.length() < 3 || productName.length() > 50) {
            throw new BbangleException(BbangleErrorCode.INVALID_PRODUCT_INFO_NOTICE_NAME);
        }
    }
}
