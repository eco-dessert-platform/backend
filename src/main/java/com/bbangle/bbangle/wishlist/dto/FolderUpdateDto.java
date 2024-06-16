package com.bbangle.bbangle.wishlist.dto;

import com.bbangle.bbangle.wishlist.validator.WishListFolderValidator;

public record FolderUpdateDto(
    String title
) {

    public FolderUpdateDto {
        WishListFolderValidator.validateTitle(title);
    }

}
