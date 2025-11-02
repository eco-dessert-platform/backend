package com.bbangle.bbangle.wishlist.customer.dto;

import com.bbangle.bbangle.wishlist.customer.validator.WishListFolderValidator;

public record FolderUpdateDto(
    String title
) {

    public FolderUpdateDto {
        WishListFolderValidator.validateTitle(title);
    }

}
