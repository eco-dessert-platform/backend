package com.bbangle.bbangle.seller.domain.model;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable // [필수] JPA 임베디드 타입 명시
@EqualsAndHashCode // [필수] 값 객체의 동등성 비교를 위해 필요
@NoArgsConstructor(access = AccessLevel.PROTECTED) // [필수] JPA 리플렉션용 기본 생성자
public class PhoneNumberVO {
    private static final Pattern PATTERN = Pattern.compile("^[0-9]{11}$");

    @Column(name = "phone", columnDefinition = "VARCHAR(20)")
    private String phoneNumber;
    @Column(name = "sub_phone", columnDefinition = "VARCHAR(100)")
    private String subPhoneNumber;

    private PhoneNumberVO(String phoneNumber, String subPhoneNumber) {
        validate(phoneNumber,subPhoneNumber);
        this.phoneNumber = phoneNumber;
        this.subPhoneNumber = subPhoneNumber;
    }


    public static PhoneNumberVO of(String phoneNumber,String subPhoneNumber) {
        return new PhoneNumberVO(phoneNumber, subPhoneNumber);
    }

    private void validate(String phoneNumber, String subPhoneNumber) {
        if (phoneNumber == null || !PATTERN.matcher(phoneNumber).matches()) {
            throw new BbangleException(BbangleErrorCode.INVALID_PHONE_NUMBER);
        }
        // subPhoneNumber는 null 허용, 값이 있다면 패턴 체크
        if (subPhoneNumber != null && !PATTERN.matcher(subPhoneNumber).matches()) {
            throw new BbangleException(BbangleErrorCode.INVALID_PHONE_NUMBER);
        }
    }

}
