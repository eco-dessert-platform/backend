package com.bbangle.bbangle.search.service.dto;

import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.constant.SortType;

public class SearchCommand {

    public record Main(
            FilterRequest filterRequest,
            SortType sort,
            String keyword,
            Long cursorId,
            Long memberId,
            Boolean isExcludedProduct
    ) {

    }

}
