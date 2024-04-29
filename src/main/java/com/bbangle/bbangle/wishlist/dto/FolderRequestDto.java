package com.bbangle.bbangle.wishlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FolderRequestDto(
    @NotBlank(message = INVALID_FOLDER_TITLE)
    @Size(max = 12, message = INVALID_FOLDER_TITLE)
    String title
) {

    private static final String INVALID_FOLDER_TITLE = "folder 제목을 확인해주십시오.";

}