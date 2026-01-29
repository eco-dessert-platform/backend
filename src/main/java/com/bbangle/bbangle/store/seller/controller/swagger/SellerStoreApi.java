package com.bbangle.bbangle.store.seller.controller.swagger;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.exception.GlobalControllerAdvice;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Seller Store", description = "(판매자) 스토어 API")
public interface SellerStoreApi {

    @Operation(
        summary = "(판매자) 스토어 검색",
        description = """
            ### 스토어 이름을 통해 스토어 목록을 조회
            - storeName에 스토어 이름을 입력하실 때 **빈칸을 제거**하고 입력하셔야합니다.
            - EX) `storeName.replaceAll(" " , "")`
            """
    )
    SingleResult<CursorPagination<SellerStoreInfo.StoreInfo>> search(
        @Parameter(description = "검색어", example = "빵그리의오븐") String storeName
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
    SingleResult<StoreResponse.StoreNameCheck> checkStoreNameDuplicate(
        @Parameter(description = "스토어명", example = "빵그리의 오븐") String storeName
    );

}
