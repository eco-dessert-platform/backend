package com.bbangle.bbangle.vat.seller.controller.dto.request;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;

public enum SellerVatExcelType {
    MONTHLY,
    DAILY,
    ORDER;

    public static SellerVatExcelType from(String value) {
        if (value == null || value.isBlank()) {
            throw new BbangleException(BbangleErrorCode.INVALID_EXCEL_TYPE);
        }

        try {
            return SellerVatExcelType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BbangleException(BbangleErrorCode.INVALID_EXCEL_TYPE, ex);
        }
    }
}
