package com.bbangle.bbangle.member.exception;

import com.bbangle.bbangle.exception.BbangleException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class UserValidator {

    private static final String INVALID_BIRTHDATE = "유효하지 않은 형식의 생년월일입니다.";
    private static final int PHONE_NUMBER_LENGTH = 11;
    private static final int MAX_NICKNAME_LENGTH = 20;

    public static void validateBirthDate(String birthDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setLenient(false);
        try {
            formatter.parse(birthDate);
        } catch (ParseException e) {
            throw new BbangleException(INVALID_BIRTHDATE);
        }
    }

    public static void validateNickname(String nickname) {
        if (nickname == null) {
            throw new BbangleException("닉네임은 필수입니다.");
        }

        if (nickname.length() > MAX_NICKNAME_LENGTH) {
            throw new BbangleException("nickname 은 20자 이하만 등록 가능합니다.");
        }
    }

    public static void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new BbangleException("전화번호는 필수입니다.");
        }
        if (phoneNumber.length() != PHONE_NUMBER_LENGTH) {
            throw new BbangleException("전화번호는 11자리로 입력해주세요");
        }
    }

}
