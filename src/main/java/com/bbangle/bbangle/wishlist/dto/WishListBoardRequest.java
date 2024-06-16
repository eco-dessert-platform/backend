package com.bbangle.bbangle.wishlist.dto;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.Objects;

public record WishListBoardRequest(
    Long folderId
) {
    public WishListBoardRequest {
        validateFolderId(folderId);
    }

    private void validateFolderId(Long folderId){
        if(Objects.isNull(folderId)){
            throw new BbangleException(BbangleErrorCode.FOLDER_ID_MUST_NOT_NULL);
        }
    }

}
