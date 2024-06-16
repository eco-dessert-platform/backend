package com.bbangle.bbangle.wishlist.dto;

import com.bbangle.bbangle.wishlist.validator.WishListFolderValidator;

public record FolderRequestDto(
    String title
) {

    public FolderRequestDto {
        WishListFolderValidator.validateTitle(title);
    }

}
