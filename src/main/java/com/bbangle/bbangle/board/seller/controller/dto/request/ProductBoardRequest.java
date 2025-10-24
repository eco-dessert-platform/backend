package com.bbangle.bbangle.board.seller.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductBoardRequest {

    public record ProductBoardSearchRequest(
        @NotBlank
        String topName,
        @NotBlank
        String subName,
        String fieldType,
        String keyword,
        @NotNull
        int page,
        @NotNull
        int size,
        @NotBlank
        String sortBy,
        @NotBlank
        String direction
    ) {

    }

}
