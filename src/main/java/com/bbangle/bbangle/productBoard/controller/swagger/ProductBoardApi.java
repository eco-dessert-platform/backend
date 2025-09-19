package com.bbangle.bbangle.productBoard.controller.swagger;

import com.bbangle.bbangle.common.page.PaginatedResponse;
import com.bbangle.bbangle.exception.ErrorResponse;
import com.bbangle.bbangle.productBoard.controller.ProductBoardRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Product-Boards", description = "상품 게시글 관련 API")
@RequestMapping("api/v1/product-boards")
public interface ProductBoardApi {

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
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    // Doc 전용 파라미터
    @Parameters({
        @Parameter(name = "topName", description = "대분류명", example = "빵"),
        @Parameter(name = "subName", description = "중분류명", example = "식빵"),
        @Parameter(name = "fieldType", description = "키워드 타입",
            schema = @Schema(allowableValues = {"PRODUCT_NAME", "ORDER_NO", "ALL"})),
        @Parameter(name = "keyword", description = "키워드", example = "건강빵"),
        @Parameter(name = "page", description = "페이지 번호", schema = @Schema(defaultValue = "0"), example = "0"),
        @Parameter(name = "size", description = "페이지 크기", schema = @Schema(defaultValue = "100"), example = "100"),
        @Parameter(name = "sortBy", description = "정렬 필드", example = "createdAt"),
        @Parameter(name = "direction", description = "정렬 방향",
            schema = @Schema(allowableValues = {"ASC", "DESC"}))
    })
    @GetMapping
    PaginatedResponse searchProductBoard(
        @Valid
        @ParameterObject
        @Parameter(hidden = true) // 실제 바인딩 DTO는 숨김
        ProductBoardRequest.ProductBoardSearchRequest request);
}
