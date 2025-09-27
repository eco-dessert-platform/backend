package com.bbangle.bbangle.order.controller;

import com.bbangle.bbangle.common.dto.ListResult;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.order.controller.dto.request.CompletedOrderFilter;
import com.bbangle.bbangle.order.controller.dto.response.CompletedOrderResponse.OrderSummary;
import com.bbangle.bbangle.order.controller.swagger.OrderApi;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController implements OrderApi {
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

}
