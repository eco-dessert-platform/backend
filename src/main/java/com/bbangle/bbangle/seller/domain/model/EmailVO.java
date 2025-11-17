package com.bbangle.bbangle.seller.domain.model;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.regex.Pattern;

public class EmailVO {
    private static final Pattern PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private final String email;

    private EmailVO(String email) {
        if (email == null || !PATTERN.matcher(email).matches()) {
            throw new BbangleException(BbangleErrorCode.INVALID_EMAIL);
        }
        this.email = email;
    }

    public static EmailVO of(String email) {
        return new EmailVO(email);
    }
}
