package com.bbangle.bbangle.order.customer.controller.swagger;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.order.customer.controller.dto.response.OrderReceiptResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Customer Order", description = "(커스터머) 주문 API")
public interface CustomerOrderApi {

    @Operation(
        summary = "(커스터머) 전자 영수증 조회",
        description = """
            주문의 전자 영수증을 조회합니다.
            - 취소(CANCEL_APPROVED), 반품(RETURN_COMPLETED) 상태 항목은 합계금액에서 제외됩니다.
            - 카드 결제가 아닌 경우 cardType, cardNumber, installment는 null을 반환합니다.
            """
    )
    SingleResult<OrderReceiptResponse> getReceipt(
        @Parameter(hidden = true) Long memberId,
        @Parameter(description = "주문 ID", required = true) @PathVariable Long orderId
    );
}
