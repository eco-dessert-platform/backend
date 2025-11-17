package com.bbangle.bbangle.seller.domain.model;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public class PhoneNumberVO {
    private static final Pattern PATTERN = Pattern.compile("^[0-9]{11}$");
    private final String phoneNumber;
    private final String subPhoneNumber;

    private PhoneNumberVO(String phoneNumber, String subPhoneNumber) {

        if (phoneNumber == null || !PATTERN.matcher(phoneNumber).matches()) {
            throw new BbangleException(BbangleErrorCode.INVALID_PHONE_NUMBER);
        }
        if( subPhoneNumber != null && !PATTERN.matcher(subPhoneNumber).matches()) {
            throw new BbangleException(BbangleErrorCode.INVALID_PHONE_NUMBER);
        }
        this.phoneNumber = phoneNumber;
        this.subPhoneNumber = subPhoneNumber;
    }

    public static PhoneNumberVO of(String phoneNumber,String subPhoneNumber) {
        return new PhoneNumberVO(phoneNumber, subPhoneNumber);
    }
}
