package com.bbangle.bbangle.seller.store.order.controller.swagger;

import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.exception.GlobalControllerAdvice;
import com.bbangle.bbangle.seller.store.order.controller.OrderResponse_v3;
import com.bbangle.bbangle.seller.store.order.controller.OrderResponse_v3.OrderItemDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Order", description = "주문 관련 API")
public interface OrderApi_v3 {

    @Operation(
        summary = "주문 품목 상세 정보 조회",
        description = "List에 주문 품목의 Id값을 받아 이를 상세 조회합니다."
    )

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "주문 품목 상세정보 조회 성공",
            content = @Content(
                schema = @Schema(implementation = OrderResponse_v3.class),
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

}
