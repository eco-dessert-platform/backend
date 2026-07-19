package com.bbangle.bbangle.order.customer.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.CustomerApiPath;
import com.bbangle.bbangle.order.customer.controller.dto.response.OrderReceiptResponse;
import com.bbangle.bbangle.order.customer.controller.swagger.CustomerOrderApi;
import com.bbangle.bbangle.order.customer.service.OrderReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(CustomerApiPath.PREFIX + "/orders")
public class OrderReceiptController implements CustomerOrderApi {

    private final ResponseService responseService;
    private final OrderReceiptService orderReceiptService;

    @Override
    @GetMapping("/{orderId}/receipt")
    public SingleResult<OrderReceiptResponse> getReceipt(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long orderId
    ) {
        OrderReceiptResponse response = orderReceiptService.getReceipt(memberId, orderId);
        return responseService.getSingleResult(response);
    }
}
