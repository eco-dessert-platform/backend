package com.bbangle.bbangle.order.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.order.controller.dto.request.CompletedOrderFilter;
import com.bbangle.bbangle.order.controller.dto.response.CompletedOrderResponse.OrderSummary;
import com.bbangle.bbangle.order.controller.swagger.SellerOrderApi_v1;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller/orders")
public class SellerOrderController_v1 implements SellerOrderApi_v1 {

    private final ResponseService responseService;

    @Override
    @GetMapping("/completed")
    public SingleResult<BbanglePageResponse<OrderSummary>> getCompletedOrders(
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
        Pageable pageable,
        CompletedOrderFilter filter,
        @AuthenticationPrincipal
        Long memberId
    ) {
        // TODO: 구현 필요
        List<OrderSummary> orderSummaries = List.of(OrderSummary.sample());
        PageImpl<OrderSummary> page = new PageImpl<>(orderSummaries, pageable, 10);
        BbanglePageResponse<OrderSummary> response = BbanglePageResponse.of(page);
        return responseService.getSingleResult(response);
    }

    @Override
    @GetMapping
    public ListResult<OrderDetail> getCompletedOrders(
        @RequestParam List<Long> orderItemIds,
        @AuthenticationPrincipal Long memberId
    ) {
        // TODO: 구현 필요
        OrderDetail orderDetail = OrderDetail.sample();
        List<OrderDetail> orderDetails = List.of(orderDetail);
        return responseService.getListResult(orderDetails);
    }

}
