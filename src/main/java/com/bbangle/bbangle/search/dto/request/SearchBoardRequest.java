package com.bbangle.bbangle.search.dto.request;

import lombok.Builder;
import org.springframework.web.bind.annotation.RequestParam;

@Builder
public record SearchBoardRequest(
        @RequestParam(value = "page")
        int page,
        @RequestParam(value = "keyword")
        String keyword,
        @RequestParam(value = "sort", required = false, defaultValue = "LATEST")
        String sort,
        @RequestParam(value = "glutenFreeTag", required = false, defaultValue = "false")
        Boolean glutenFreeTag,
        @RequestParam(value = "highProteinTag", required = false, defaultValue = "false")
        Boolean highProteinTag,
        @RequestParam(value = "sugarFreeTag", required = false, defaultValue = "false")
        Boolean sugarFreeTag,
        @RequestParam(value = "veganTag", required = false, defaultValue = "false")
        Boolean veganTag,
        @RequestParam(value = "ketogenicTag", required = false, defaultValue = "false")
        Boolean ketogenicTag,
        @RequestParam(value = "orderAvailableToday", required = false, defaultValue = "false")
        Boolean orderAvailableToday,
        @RequestParam(value = "category", required = false, defaultValue = "")
        String category,
        @RequestParam(value = "minPrice", required = false, defaultValue = "0")
        Integer minPrice,
        @RequestParam(value = "maxPrice", required = false, defaultValue = "0")
        Integer maxPrice
) {
}
