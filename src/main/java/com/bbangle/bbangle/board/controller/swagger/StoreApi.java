package com.bbangle.bbangle.board.controller.swagger;

import com.bbangle.bbangle.board.controller.dto.StoreResponse.SearchResponse;
import com.bbangle.bbangle.common.dto.ListResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Store", description = "스토어 API")
public interface StoreApi {

    @Operation(summary = "스토어 검색")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "판매자 생성 성공",
            content = @Content(
                schema = @Schema(implementation = ListResult.class),
                examples = @ExampleObject(
                    name = "successResponse",
                    summary = "성공응답 예시",
                    value = """
                        {
                          "success": true,
                          "code": 0,
                          "message": "SUCCESS",
                          "list": [
                            {
                              "storeId": 1,
                              "storeName": "빵그리의 오븐 즉석빵 상점"
                            },
                            {
                              "storeId": 2,
                              "storeName": "빵그리의 오븐 공장빵 상점"
                            }
                          ]
                        }
                        """
                )
            )
        )
    })
    ListResult<SearchResponse> search(
        @Parameter(description = "검색어", example = "빵그리의 오븐") String searchValue
    );

}
