package com.bbangle.bbangle.wishlist.customer.dto;

import com.bbangle.bbangle.wishlist.customer.validator.WishListFolderValidator;

public record FolderRequestDto(
    String title
) {

    public FolderRequestDto {
        WishListFolderValidator.validateTitle(title);
    }

}
