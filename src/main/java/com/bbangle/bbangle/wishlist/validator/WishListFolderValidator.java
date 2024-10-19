package com.bbangle.bbangle.wishlist.validator;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.member.domain.Member;
import java.util.Objects;

public class WishListFolderValidator {

    private static final String DEFAULT_FOLDER_NAME = "기본 폴더";

    public static void validateTitle(String title) {
        if (Objects.isNull(title) || title.isBlank() || title.length() > 12) {
            throw new BbangleException(BbangleErrorCode.INVALID_FOLDER_TITLE);
        }

        if(title.equals(DEFAULT_FOLDER_NAME)){
            throw new BbangleException(BbangleErrorCode.DEFAULT_FOLDER_NAME_USED);
        }
    }

    public static void validateMember(Member member) {
        if(Objects.isNull(member)){
            throw new BbangleException(BbangleErrorCode.INVALID_FOLDER_MEMBER);
        }
    }

}
