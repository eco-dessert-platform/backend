package com.bbangle.bbangle.search.dto.response;

import com.bbangle.bbangle.store.dto.StoreResponseDto;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Builder;

@Builder
public record SearchStoreResponse(
        List<StoreResponseDto> content,
        int pageNumber,
        int itemAllCount,
        int limitItemCount,
        int currentItemCount,
        boolean existNextPage
){

    public static SearchStoreResponse getEmpty(int pageNumber, int limitItemCount){
        return SearchStoreResponse.builder().content(Collections.emptyList())
                .itemAllCount(0)
                .pageNumber(pageNumber)
                .limitItemCount(limitItemCount)
                .currentItemCount(0)
                .existNextPage(false)
                .build();
    }
}
