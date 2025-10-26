package com.bbangle.bbangle.search.service.dto;

import com.bbangle.bbangle.board.customer.domain.constant.SortType;
import com.bbangle.bbangle.board.customer.dto.FilterRequest;

public class SearchCommand {

    public record Main(
        FilterRequest filterRequest,
        SortType sort,
        String keyword,
        Long cursorId,
        Long memberId,
        Boolean isExcludedProduct,
        Integer limitSize
    ) {

    }

}
