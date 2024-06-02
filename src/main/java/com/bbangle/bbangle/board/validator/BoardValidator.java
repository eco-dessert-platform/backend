package com.bbangle.bbangle.board.validator;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardValidator {

    public static void validateMinPrice(Integer minPrice) {
        if (Objects.nonNull(minPrice) && minPrice < 0) {
            throw new BbangleException(BbangleErrorCode.PRICE_NOT_OVER_ZERO);
        }
    }

    public static void validateMaxPrice(Integer maxPrice) {
        if (Objects.nonNull(maxPrice) && maxPrice < 0) {
            throw new BbangleException(BbangleErrorCode.PRICE_NOT_OVER_ZERO);
        }
    }

    public static void validateListNotEmpty(List result, BbangleErrorCode errorCode) {
        if (result.isEmpty()) {
            throw new BbangleException(errorCode);
        }
    }

    public static void validateNotNull(Object object, BbangleErrorCode errorCode) {
        if (Objects.isNull(object)) {
            throw new BbangleException(errorCode);
        }
    }
}
