package com.bbangle.bbangle.search.validation;

import static com.bbangle.bbangle.exception.BbangleErrorCode.EMPTY_KEYWORD;

import com.bbangle.bbangle.exception.BbangleException;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchValidation {

    public static void checkNullOrEmptyKeyword(String keyword) {
        if (Objects.isNull(keyword) || keyword.trim().isEmpty()) {
            throw new BbangleException(EMPTY_KEYWORD);
        }
    }
}
