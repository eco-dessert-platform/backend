package com.bbangle.bbangle.board.seller.controller.swagger;

import com.bbangle.bbangle.board.seller.controller.dto.request.CreateBoardRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.ProductBoardRequest.ProductBoardSearchRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.UpdateBoardRequest;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardListResponse;
import com.bbangle.bbangle.board.seller.service.info.BoardInfo;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.exception.GlobalControllerAdvice;
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
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "SellerBoards", description = "판매자 상품 게시글 관련 API")
public interface SellerBoardApi {

    @Operation(summary = "상품 게시글 등록")
    SingleResult<BoardInfo> createBoard(
        @Parameter(hidden = true) @AuthenticationPrincipal Long sellerId,
        @Valid @ModelAttribute CreateBoardRequest request
    );

    @Operation(
        summary = "판매자 상품 게시글 조회",
        description = """
            ### 판매자 본인 스토어의 상품 게시글 목록을 조회합니다.
            - 필터 없이 요청 시 전체 게시글을 반환합니다.
            - `saleStatus` 필터를 적용하면 해당 판매 상태의 게시글만 반환됩니다.
            - `tabCounts`는 필터 여부와 무관하게 스토어 전체 판매상태별 게시글 수를 반환합니다.
            - `inventoryStatus`는 하위 상품 옵션의 품절 여부를 집계하여 계산합니다.
              - `IN_STOCK`: 모든 옵션 재고 있음
              - `PARTIAL_SOLDOUT`: 일부 옵션 품절
              - `SOLDOUT`: 모든 옵션 품절
            - `deliveryType`은 배송비(`deliveryFee`)와 무료배송 조건(`freeShippingConditions`)으로 계산합니다.
              - `FREE`: 배송비 없음
              - `CONDITIONAL_FREE`: 배송비 있고 무료배송 조건 있음
              - `PAID`: 배송비 있고 무료배송 조건 없음
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "상품 게시글 조회 성공",
            content = @Content(
                schema = @Schema(implementation = SingleResult.class),
                examples = @ExampleObject(
                    name = "successResponse",
                    summary = "성공응답 예시",
                    value = """
                        {
                            "success": true,
                            "code": 0,
                            "message": "SUCCESS",
                            "content": {
                                "tabCounts": {
                                    "ON_SALE": 5,
                                    "OUT_OF_STOCK": 2,
                                    "STOPPED": 1,
                                    "PENDING": 3,
                                    "BANNED": 0
                                },
                                "boards": {
                                    "content": [
                                        {
                                            "boardId": 1,
                                            "thumbnailUrl": "https://cdn.bbangle.com/board-images/thumb.jpg",
                                            "title": "글루텐프리 식빵 세트",
                                            "inventoryStatus": "IN_STOCK",
                                            "price": 15000,
                                            "discountPrice": 13500,
                                            "discountValue": 10,
                                            "deliveryFee": 3000,
                                            "freeShippingConditions": 30000,
                                            "deliveryType": "CONDITIONAL_FREE",
                                            "saleStatus": "ON_SALE"
                                        }
                                    ],
                                    "pageNumber": 0,
                                    "pageSize": 10,
                                    "totalPages": 1,
                                    "totalElements": 1
                                }
                            }
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
    @Parameters({
        @Parameter(name = "saleStatus", description = "판매상태 탭 필터 (null=전체)",
            schema = @Schema(allowableValues = {"ON_SALE", "OUT_OF_STOCK", "STOPPED", "PENDING", "BANNED"})),
        @Parameter(name = "mainCategory", description = "대분류 (null=전체)",
            schema = @Schema(allowableValues = {"BREAD", "SNACK"})),
        @Parameter(name = "category", description = "중분류 (null=전체)",
            schema = @Schema(allowableValues = {"BREAD", "BAGEL", "CAKE", "COOKIE", "JAM", "GRANOLA", "TART", "SNACK", "ICE_CREAM", "YOGURT", "ETC"})),
        @Parameter(name = "keyword", description = "상품명 검색어"),
        @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", schema = @Schema(defaultValue = "0")),
        @Parameter(name = "size", description = "페이지 크기", schema = @Schema(defaultValue = "10")),
        @Parameter(name = "sortBy", description = "정렬 기준 (기본값: LATEST)",
            schema = @Schema(allowableValues = {"LATEST", "OLDEST", "NAME"}))
    })
    SingleResult<SellerBoardListResponse> searchProductBoard(
        @Parameter(hidden = true) Long sellerId,
        @Parameter(hidden = true)
        @ParameterObject
        ProductBoardSearchRequest request);

    @Operation(
        summary = "판매자 상품 게시글 수정",
        description = "상품 게시글을 수정합니다. multipart/form-data 형식으로 전송합니다. " +
            "thumbnailImgFile이 없으면 existingThumbnailUrl로 기존 썸네일을 유지합니다. " +
            "products[].productId가 null이면 신규 상품으로 추가됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "상품 게시글 수정 성공",
            content = @Content(
                schema = @Schema(implementation = SingleResult.class),
                examples = @ExampleObject(
                    name = "successResponse",
                    summary = "성공응답 예시",
                    value = """
                        {
                            "success": true,
                            "code": 0,
                            "message": "SUCCESS",
                            "content": {
                                "boardId": 1,
                                "title": "수정된 상품명",
                                "createdAt": "2024-01-01T00:00:00"
                            }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "해당 게시글에 대한 접근 권한 없음",
            content = @Content(schema = @Schema(implementation = GlobalControllerAdvice.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "게시글을 찾을 수 없음",
            content = @Content(schema = @Schema(implementation = GlobalControllerAdvice.class))
        )
    })
    SingleResult<BoardInfo> changeProductBoard(
        @Parameter(hidden = true) @AuthenticationPrincipal Long sellerId,
        @Parameter(name = "boardId", description = "수정할 게시글 ID", example = "1") Long boardId,
        @Valid @ModelAttribute UpdateBoardRequest request);


    @Operation(
        summary = "판매자 상품 게시글 복제",
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

    @Operation(
        summary = "판매자 상품 게시글 삭제",
        description = "상품 게시글을 삭제합니다, List로 여러개의 ID 값을 받아 처리할 수있습니다."
    )

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "상품 게시글 삭제 성공",
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
    CommonResult removeProductBoards(
        @Parameter(name = "storeId", description = "스토어 ID", example = "1")
        Long storeId,
        List<Long> boardIds);

}
