package com.bbangle.bbangle.seller.order.controller.swagger;

import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.page.PaginatedResponse;
import com.bbangle.bbangle.exception.ErrorResponse;
import com.bbangle.bbangle.exception.GlobalControllerAdvice;
import com.bbangle.bbangle.seller.order.controller.dto.request.CompletedOrderFilter;
import com.bbangle.bbangle.seller.order.controller.dto.request.OrderRequest;
import com.bbangle.bbangle.seller.order.controller.dto.response.CompletedOrderResponse.OrderSummary;
import com.bbangle.bbangle.seller.order.controller.dto.response.OrderDetailResponse.OrderDetail;
import com.bbangle.bbangle.seller.order.controller.dto.response.OrderResponse.OrderItemDetailResponse;
import com.bbangle.bbangle.seller.order.controller.dto.response.OrderResponse.OrderSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Seller Order", description = "(판매자) 주문 API")
public interface SellerOrderApi {

    @Operation(summary = "(판매자) 완료주문내역 페이징 조회")
    SingleResult<BbanglePageResponse<OrderSummary>> getCompletedOrders(
        @ParameterObject Pageable pageable,
        @ParameterObject CompletedOrderFilter filter,
        Long memberId
    );

    @Operation(summary = "(판매자) 주문 상세 조회")
    ListResult<OrderDetail> getCompletedOrders(
        List<Long> orderItemIds,
        Long memberId
    );

    // TODO: v3
    @Operation(
        summary = "주문 품목 상세 정보 조회",
        description = "List에 주문 품목의 Id값을 받아 이를 상세 조회합니다."
    )

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "주문 품목 상세정보 조회 성공",
            content = @Content(
                schema = @Schema(implementation = OrderItemDetailResponse.class),
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
                              "orderNumber": "ORDER-2025-04-05-00001",
                              "orderInfo": {
                                "orderDate": "2025-04-05",
                                "orderStatusLabel": "반품-상품발송"
                              },
                              "buyer": {
                                "recipientName": "홍길동",
                                "buyerName": "홍길동",
                                "buyerPhone1": "010-1234-5678",
                                "buyerPhone2": "010-9876-5432"
                              },
                              "shipping": {
                                "statusLabel": "수거중",
                                "courierCompany": "CJ대한통운",
                                "trackingNumber": "1234-5678-910",
                                "shippingFee": 3000,
                                "address": "서울시 강남구 예제로 123",
                                "memo": "문 앞에 두세요."
                              },
                              "orderItem": {
                                "boardTitle": "예제 상품",
                                "itemName": "예제 상품",
                                "quantity": 2,
                                "unitPrice": 50000,
                                "totalPrice": 100000
                              }
                            }
                        
                          ],
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
    ListResult<OrderItemDetailResponse> searchDetailItems(
        List<Long> orderItemList);


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
    PaginatedResponse<OrderSearchResponse> searchOrders(
        @Parameter(description = "판매자 ID", example = "1")
        Long sellerId,
        @Parameter(hidden = true)
        @ParameterObject
        @Valid
        OrderRequest.OrderSearchRequest request);

}
