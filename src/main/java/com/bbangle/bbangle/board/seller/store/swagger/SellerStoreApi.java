package com.bbangle.bbangle.board.seller.store.swagger;

import com.bbangle.bbangle.board.seller.store.dto.StoreResponse.SearchResponse;
import com.bbangle.bbangle.common.dto.ListResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Seller Store", description = "(판매자) 스토어 API")
public interface SellerStoreApi {

    @Operation(summary = "(판매자) 스토어 검색")
    ListResult<SearchResponse> search(
        @Parameter(description = "검색어", example = "빵그리의 오븐") String searchValue
    );

}
