package com.bbangle.bbangle.order.customer.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.order.customer.dto.request.OrderCreateRequest;
import com.bbangle.bbangle.order.customer.dto.request.OrderPreviewRequest;
import com.bbangle.bbangle.order.customer.dto.response.OrderCreateResponse;
import com.bbangle.bbangle.order.customer.dto.response.OrderPreviewResponse;
import com.bbangle.bbangle.order.customer.service.CustomerOrderCreateService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer/orders")
@RequiredArgsConstructor
public class CustomerOrderCreateController {

    private final CustomerOrderCreateService customerOrderCreateService;
    private final ResponseService responseService;

    @PostMapping("/preview")
    @Operation(summary = "주문서 미리보기")
    public SingleResult<OrderPreviewResponse> getOrderPreview(
        @RequestBody @Valid OrderPreviewRequest request,
        @AuthenticationPrincipal Long memberId
    ) {
        OrderPreviewResponse response = customerOrderCreateService.getOrderPreview(memberId, request);
        return responseService.getSingleResult(response);
    }

    @PostMapping
    @Operation(summary = "주문 생성")
    public SingleResult<OrderCreateResponse> createOrders(
        @RequestBody @Valid OrderCreateRequest request,
        @AuthenticationPrincipal Long memberId
    ) {
        OrderCreateResponse response = customerOrderCreateService.createOrders(memberId, request);
        return responseService.getSingleResult(response);
    }
}
