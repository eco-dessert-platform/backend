package com.bbangle.bbangle.seller.store.order.controller;

import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.store.order.controller.OrderResponse.OrderItemDetailResponse;
import com.bbangle.bbangle.seller.store.order.controller.swagger.OrderApi;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/seller")
public class OrderController implements OrderApi {

    private final ResponseService responseService;

    @Override
    @PostMapping("/orders/items")
    public ListResult<OrderItemDetailResponse> searchDetailItems(
        @RequestBody
        List<Long> orderItemList) {

        List<OrderResponse.OrderItemDetailResponse> responses = orderItemList.stream()
            .map(id -> new OrderResponse.OrderItemDetailResponse(
                // 주문번호
                "ORDER-2025-04-05-test",

                // 주문 정보
                new OrderResponse.OrderItemDetailResponse.OrderInfo(
                    "2025-04-05",                // orderDate (String, yyyy-MM-dd)
                    "반품-상품발송"                 // orderStatusLabel
                ),

                // 주문자 정보
                new OrderResponse.OrderItemDetailResponse.BuyerInfo(
                    "홍길동",                     // recipientName
                    "홍길동",                     // buyerName
                    "010-1234-5678",             // buyerPhone1
                    "010-9876-5432"              // buyerPhone2
                ),

                // 배송 정보
                new OrderResponse.OrderItemDetailResponse.ShippingInfo(
                    "수거중",                     // statusLabel
                    "CJ대한통운",                 // courierCompany
                    "1234-5678-910",             // trackingNumber
                    3000L,                       // shippingFee
                    "서울시 강남구 예제로 123",    // address
                    "문 앞에 두세요."              // memo
                ),

                // 주문 상품
                new OrderResponse.OrderItemDetailResponse.OrderItem(
                    "예제 상품",                  // boardTitle
                    "예제 상품",                  // itemName
                    2,                           // quantity
                    50_000L,                     // unitPrice
                    100_000L                     // totalPrice
                )
            ))
            .toList();
        return responseService.getListResult(responses);
    }
}
