package com.bbangle.bbangle.order.customer.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.CustomerApiPath;
import com.bbangle.bbangle.order.customer.controller.dto.request.CreateOrderRequest;
import com.bbangle.bbangle.order.customer.controller.dto.response.CreateOrderResponse;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerOrderDetail;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderPageResponse;
import com.bbangle.bbangle.order.customer.service.CustomerOrderCreateService;
import com.bbangle.bbangle.order.customer.service.CustomerOrderService;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderDetailCommand;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderSearchCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(CustomerApiPath.PREFIX + "/orders")
public class CustomerOrderController implements CustomerOrderApi {

    private final ResponseService responseService;
    private final CustomerOrderService customerOrderService;
    private final CustomerOrderCreateService customerOrderCreateService;

    /**
     * 주문 생성. 스토어별로 주문을 나누고 결제 1건으로 묶습니다.
     */
    @Override
    @PostMapping
    public SingleResult<CreateOrderResponse> createOrder(
        @AuthenticationPrincipal Long memberId,
        @RequestHeader("X-Transaction-Id") String transactionId,
        @Valid @RequestBody CreateOrderRequest request
    ) {
        CreateOrderCommand command = CreateOrderCommand.of(memberId, transactionId, request);

        CreateOrderResponse response = customerOrderCreateService.create(command);

        return responseService.getSingleResult(response);
    }

    /**
     * 소비자 주문목록 조회. 기본 정렬은 주문일(orderDate) 최신순입니다.
     */
    @Override
    @GetMapping
    public SingleResult<CustomerOrderPageResponse> getOrders(
        @AuthenticationPrincipal Long memberId,
        @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        CustomerOrderSearchCommand command = CustomerOrderSearchCommand.builder()
            .memberId(memberId)
            .pageable(pageable)
            .build();

        CustomerOrderPageResponse response = customerOrderService.getOrders(command);

        return responseService.getSingleResult(response);
    }

    /**
     * 소비자 주문 상세 조회. 본인 소유의 단일 주문만 조회할 수 있습니다.
     */
    @Override
    @GetMapping("/{orderId}")
    public SingleResult<CustomerOrderDetail> getOrderDetail(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long orderId
    ) {
        CustomerOrderDetailCommand command = CustomerOrderDetailCommand.builder()
            .memberId(memberId)
            .orderId(orderId)
            .build();

        CustomerOrderDetail response = customerOrderService.getOrderDetail(command);

        return responseService.getSingleResult(response);
    }
}
