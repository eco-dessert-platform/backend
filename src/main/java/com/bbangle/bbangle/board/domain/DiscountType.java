package com.bbangle.bbangle.board.domain;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiscountType {
    RATE("퍼센트"),
    AMOUNT("원");

    private final String description;

    public static DiscountType from(String value) {
        return Arrays.stream(values())
            .filter(type -> type.name().equals(value))
            .findFirst()
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.INVALID_DISCOUNT_TYPE));
    }
}
