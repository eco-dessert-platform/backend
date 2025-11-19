package com.bbangle.bbangle.store.seller.controller.swagger;

import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.StoreCustomPage;
import com.bbangle.bbangle.exception.GlobalControllerAdvice;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SearchResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreNameCheckResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Seller Store", description = "(판매자) 스토어 API")
public interface SellerStoreApi {

    @Operation(summary = "(판매자) 스토어 검색")
    ListResult<SearchResponse> search(
        @Parameter(description = "검색어", example = "빵그리의 오븐") String searchValue
    );

    @Operation(summary = "스토어명 중복 확인")
    @ApiResponses(value = {
              @ApiResponse(
            responseCode = "400",
            description = "유효하지 않은 스토어명",
            content = @Content(
                schema = @Schema(implementation = GlobalControllerAdvice.class)
            )
        )
    })
    SingleResult<StoreCustomPage<List<StoreNameCheckResponse>>> checkStoreNameDuplicate(
        @Parameter(description = "스토어명", example = "빵그리의 오븐") String storeName
    );

}
