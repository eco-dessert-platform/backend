package com.bbangle.bbangle.order.controller;

import static com.bbangle.bbangle.order.controller.dto.CompletedOrderStatus.CANCELED;
import static com.bbangle.bbangle.order.controller.dto.CompletedOrderStatus.PURCHASED;
import static com.bbangle.bbangle.order.controller.dto.DayOfWeek.MONDAY;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.order.controller.dto.request.CompletedOrderFilter;
import com.bbangle.bbangle.order.controller.dto.response.CompletedOrderResponse.OrderSummary;
import com.bbangle.bbangle.order.controller.swagger.OrderApi;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDateTime;
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
        OrderSummary.OrderItem orderItem1 = OrderSummary.OrderItem.of(1L, PURCHASED, "CJ대한통운",
            "123-123", "저칼로리 베이글", 5);
        OrderSummary.OrderItem orderItem2 = OrderSummary.OrderItem.of(2L, CANCELED, "롯데택배",
            "123-456", "저당 초콜릿", 10);
        OrderSummary orderSummary = OrderSummary.of(1L, "000-123", LocalDateTime.of(2024, 1, 1, 12, 0),
            MONDAY, "홍길동", List.of(orderItem1, orderItem2));
        List<OrderSummary> orderSummaries = List.of(orderSummary);
        PageImpl<OrderSummary> page = new PageImpl<>(orderSummaries, pageable, 10);
        BbanglePageResponse<OrderSummary> response = BbanglePageResponse.of(page);
        return responseService.getSingleResult(response);
    }

}
