package com.bbangle.bbangle.order.controller;

import static com.bbangle.bbangle.order.controller.dto.CompletedOrderStatus.CANCELED;
import static com.bbangle.bbangle.order.controller.dto.CompletedOrderStatus.PURCHASED;
import static com.bbangle.bbangle.order.controller.dto.DayOfWeek.MONDAY;
import static com.bbangle.bbangle.order.controller.dto.DayOfWeek.TUESDAY;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.order.controller.dto.request.CompletedOrderFilter;
import com.bbangle.bbangle.order.controller.dto.response.CompletedOrderResponse.OrderSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order", description = "주문 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final ResponseService responseService;

    @Operation(summary = "완료주문내역 페이징 조회")
    @GetMapping("/completed")
    public SingleResult<BbanglePageResponse<OrderSummary>> getCompletedOrders(
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
        @ParameterObject
        Pageable pageable,
        @ParameterObject CompletedOrderFilter filter
    ) {
        // TODO: 구현 필요
        List<OrderSummary> orderSummaries = List.of(
            new OrderSummary(PURCHASED, "0000-1231", "CJ대한통운", "123-123",
                LocalDateTime.of(2024, 1, 1, 12, 0), MONDAY, "홍길동", "저칼로리 베이글", 5),
            new OrderSummary(CANCELED, "0000-1234", "롯데택배", "123-159",
                LocalDateTime.of(2024, 1, 1, 12, 0), TUESDAY, "김영희", "저당 초콜릿", 10)
        );
        PageImpl<OrderSummary> page = new PageImpl<>(orderSummaries, pageable, 10);
        BbanglePageResponse<OrderSummary> response = BbanglePageResponse.of(page);
        return responseService.getSingleResult(response);
    }

}
