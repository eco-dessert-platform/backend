package com.bbangle.bbangle.order.controller.swagger;


import com.bbangle.bbangle.common.page.PaginatedResponse;
import com.bbangle.bbangle.exception.ErrorResponse;
import com.bbangle.bbangle.order.controller.OrderRequest;
import com.bbangle.bbangle.order.controller.OrderResponse.OrderSearchResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Order", description = "주문 관련 API")
@RequestMapping("api/v1/orders")
public interface OrderApi {

    @Operation(
        summary = "주문 품목 조회",
        description = "페이징 처리된 판매자의 전체 주문 품목을 조회합니다."
    )

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "주문 품목 조회 성공",
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
                              "orderStatus": "PAID",
                              "deliveryStatus": "PREPARING_PRODUCT",
                              "courierCompany": "CJ 대한통운",
                              "trackingNumber": "1234567890",
                              "orderNumber": "ORDER-20240921-00001",
                              "paymentAt": "2025-09-21T23:00:29.2508851",
                              "recipientName": "홍길동",
                              "itemName": "맛있는 식빵"
                            },
                            {
                              "orderStatus": "PAID",
                              "deliveryStatus": "SHIPPING",
                              "courierCompany": "우체국 택배",
                              "trackingNumber": "0987654321",
                              "orderNumber": "ORDER-20240921-00002",
                              "paymentAt": "2025-09-21T22:00:29.2518844",
                              "recipientName": "김철수",
                              "itemName": "달콤한 소금빵"
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

    @Parameters({
        @Parameter(name = "startDate", description = "시작일", example = "2025-08-11", required = true),
        @Parameter(name = "endDate", description = "종료일", example = "2025-08-12", required = true),
        @Parameter(name = "deliveryStatus", description = "배송상태", schema = @Schema(allowableValues = {
            "ALL", "PAYMENT_COMPLETED", "PREPARING_ORDER", "SHIPPED",
            "DELIVERED"}, defaultValue = "ALL"), required = true),
        @Parameter(name = "fieldType", description = "키워드 타입",
            schema = @Schema(allowableValues = {"ORDER_NUMBER", "BUYER", "ITEM_NAME",
                "TRACKING_NUMBER"}, defaultValue = "ORDER_NUMBER")),
        @Parameter(name = "keyword", description = "키워드", example = "건강빵"),
        @Parameter(name = "page", description = "페이지 번호", schema = @Schema(defaultValue = "0"), example = "0", required = true),
        @Parameter(name = "size", description = "페이지 크기", schema = @Schema(defaultValue = "100"), example = "100", required = true),
    })
    @GetMapping("/seller")
    PaginatedResponse<OrderSearchResponse> searchOrders(
        @Valid
        @RequestParam(name = "sellerId")
        Long sellerId,
        @Parameter(hidden = true)
        @ParameterObject
        OrderRequest.OrderSearchRequest request);


}
