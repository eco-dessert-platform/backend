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

    private String productName;
    private String foodType;
    private String manufacturer;
    private String originLocation;
    private String manufactureDate;
    private String expirationDate;
    private String storageGuide;
    private String packagingQuantityUnit;
    private String rawMaterialName;
    private String nutritionInfo;
    private String transgenic;
    private String customerWarning;
    private String importFood;

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
