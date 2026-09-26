package com.bbangle.bbangle.order.customer.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.order.customer.controller.dto.request.CreateOrderRequest;
import com.bbangle.bbangle.order.customer.controller.dto.response.CreateOrderResponse;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerOrderDetail;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Customer Order", description = "(소비자) 주문 API")
public interface CustomerOrderApi {

    @Operation(
        summary = "(소비자) 주문 생성",
        description = """
            주문서 화면의 [결제하기] 진입 시 호출합니다. 결제 전 주문을 만들고 결제금액을 확정합니다.

            - 주문 상품은 스토어 구분 없이 평평하게 보냅니다. 서버가 스토어별로 나눠 주문을 생성합니다.
              (결제 1건 : 주문 N건)
            - 모든 금액은 서버가 계산합니다. `expectedTotalAmount` 는 검증용이며 서버 계산값과 다르면 주문이 거부됩니다.
            - 생성된 주문은 `PAYMENT_PENDING` 상태이며 주문목록에는 노출되지 않습니다.
            - 재고는 검증만 하고 차감하지 않습니다. 차감은 결제 승인 시점에 이루어집니다.
            - 응답의 `paymentNumber` · `orderName` · `totalAmount` 를 그대로 PG 결제창에 전달합니다.
            - X-Transaction-Id 헤더로 중복 요청을 방지합니다. 같은 값으로 5분 내 재요청하면 거부됩니다.
            """
    )
    SingleResult<CreateOrderResponse> createOrder(
        @Parameter(hidden = true) Long memberId,

        @Parameter(
            description = "중복 요청 방지용 고유 거래 ID (UUID, 요청 시 생성)",
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true
        )
        String transactionId,

        CreateOrderRequest request
    );

    @Operation(
        summary = "(소비자) 주문목록 조회",
        description = "인증된 회원의 주문을 주문일 최신순으로 페이징 조회합니다. "
            + "각 주문상품의 진행 단계(일반/반품/교환/취소)와 탭별 카운트(statusCounts)가 포함됩니다."
    )
    SingleResult<CustomerOrderPageResponse> getOrders(
        Long memberId,
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "(소비자) 주문 상세 조회",
        description = "인증된 회원 본인 소유의 단일 주문 상세를 조회합니다. "
            + "결제금액(반품·취소 제외), 배송지, 주문상품별 진행 단계와 상태 뱃지/할인율/태그를 포함합니다."
    )
    SingleResult<CustomerOrderDetail> getOrderDetail(
        Long memberId,
        @Parameter(description = "주문 ID", example = "1") Long orderId
    );
}
