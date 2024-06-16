package com.bbangle.bbangle.wishlist.dto;

import java.util.List;

public record FolderResponseDto(
    Long folderId,
    String title,
    int count,
    List<String> productImages
) {

}
