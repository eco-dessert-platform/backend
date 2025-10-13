package com.bbangle.bbangle.seller.store.board.controller.swagger;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.page.PaginatedResponse;
import com.bbangle.bbangle.exception.GlobalControllerAdvice;
import com.bbangle.bbangle.seller.store.board.controller.dto.ProductBoardRequest_v3;
import com.bbangle.bbangle.seller.store.board.controller.dto.ProductBoardResponse_v3.ProductBoardSearchResponse;
import com.bbangle.bbangle.seller.store.board.controller.dto.ProductBoardUpdateRequest_v3;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "ProductBoards", description = "상품 게시글 관련 API")
public interface ProductBoardApi_v3 {

    @Operation(
        summary = "상품 게시글 조회",
        description = "페이징 처리된 상품 게시글을 조회합니다. 페이징은 100개 단위로 구현되어집니다"
    )

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "상품 게시글 조회 성공",
            content = @Content(
                schema = @Schema(implementation = PaginatedResponse.class),
                examples = @ExampleObject(
                    name = "successResponse",
                    summary = "성공응답 예시",
                    value = """
                        {
                            "success": true,
                            "code": 0,
                            "message": "SUCCESS",
                            "content": [
                                {   
                                    "productId": 1,
                                    "imgUrl": "https://image.bbangle.com/...",
                                    "productName": "맛있는 식빵",
                                    "inventoryStatus": "재고 있음",
                                    "price": 5000,
                                    "discountPrice": 4500,
                                    "fee": 3000,
                                    "freeMinPrice": 30000,
                                    "saleStatus": "ON_SALE"
                                }
                            ],
                            "pageNumber": 0,
                            "pageSize": 100,
                            "totalPages": 5,
                            "totalElements": 480
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(
                schema = @Schema(implementation = GlobalControllerAdvice.class)
            )
        )
    })
    // Doc 전용 파라미터
    @Parameters({
        @Parameter(name = "storeId", description = "스토어 ID", example = "1"),
        @Parameter(name = "topName", description = "대분류명", example = "빵", required = true),
        @Parameter(name = "subName", description = "중분류명", example = "식빵", required = true),
        @Parameter(name = "fieldType", description = "키워드 타입",
            schema = @Schema(allowableValues = {"PRODUCT_NAME", "ORDER_NO", "ALL"})),
        @Parameter(name = "keyword", description = "키워드", example = "건강빵"),
        @Parameter(name = "page", description = "페이지 번호", schema = @Schema(defaultValue = "0"), example = "0", required = true),
        @Parameter(name = "size", description = "페이지 크기", schema = @Schema(defaultValue = "100"), example = "100", required = true),
        @Parameter(name = "sortBy", description = "정렬 필드", example = "createdAt", required = true),
        @Parameter(name = "direction", description = "정렬 방향", schema = @Schema(allowableValues = {
            "ASC", "DESC"}), required = true)
    })
    PaginatedResponse<ProductBoardSearchResponse> searchProductBoard(
        Long storeId,
        @Parameter(hidden = true) // 실제 바인딩 DTO는 숨김
        @ParameterObject
        ProductBoardRequest_v3.ProductBoardSearchRequest request);

    @Operation(summary = "상품 게시글 수정", description = "상품 게시글을 수정합니다.")

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "상품 게시글 수정",
            content = @Content(schema = @Schema(implementation = CommonResult.class),
                examples = @ExampleObject(name = "successResponse",
                    summary = "성공응답 예시",
                    value = """
                        {
                            "success": true,
                            "code": 0,
                            "message": "SUCCESS",
                        }
                        """))),
        @ApiResponse(
            responseCode = "400", description = "잘못된 요청 데이터",
            content = @Content(schema = @Schema(implementation = GlobalControllerAdvice.class)
            )
        )
    }
    )
    CommonResult changeProductBoard(
        @Parameter(name = "storeId", description = "스토어 ID", example = "1") Long storeId,
        @Parameter(name = "boardId", description = "게시글 ID", example = "1") Long boardId,
        ProductBoardUpdateRequest_v3 request);


    @Operation(
        summary = "상품 게시글 복제",
        description = "상품 게시글을 복제 합니다."
    )

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "상품 게시글 복제",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @ExampleObject(
                    name = "successResponse",
                    summary = "성공응답 예시",
                    value = """
                        {
                            "success": true,
                            "code": 0,
                            "message": "SUCCESS",
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(
                schema = @Schema(implementation = GlobalControllerAdvice.class)
            )
        )
    })
    CommonResult copyProductBoard(
        @Parameter(name = "storeId", description = "스토어 ID", example = "1")
        Long storeId,
        @Parameter(name = "boardId", description = "게시글 ID", example = "1")
        Long boardId);

}
