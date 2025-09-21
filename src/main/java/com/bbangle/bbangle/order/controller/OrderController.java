package com.bbangle.bbangle.order.controller;


import com.bbangle.bbangle.common.page.PaginatedResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.order.controller.OrderRequest.OrderSearchRequest;
import com.bbangle.bbangle.order.controller.OrderResponse.OrderSearchResponse;
import com.bbangle.bbangle.order.controller.swagger.OrderApi;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final ResponseService responseService;

    @Override
    public PaginatedResponse<OrderSearchResponse> searchOrders(Long sellerId,
        OrderSearchRequest request) {

        // Mock 데이터 생성
        List<OrderSearchResponse> mockOrders = List.of(
            new OrderSearchResponse(
                "PAID",
                "PREPARING_PRODUCT",
                "CJ 대한통운",
                "1234567890",
                "ORDER-20240921-00001",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                "홍길동",
                "맛있는 식빵"
            ),
            new OrderSearchResponse(
                "PAID",
                "SHIPPING",
                "우체국 택배",
                "0987654321",
                "ORDER-20240921-00002",
                LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_DATE_TIME),
                "김철수",
                "달콤한 소금빵"
            )
        );

        PageRequest pageable = PageRequest.of(request.page(), request.size());
        // 전체 아이템 개수는 480개로 가정
        Page<OrderSearchResponse> resultPage = new PageImpl<>(mockOrders, pageable, 480);

        PaginatedResponse<OrderSearchResponse> response = new PaginatedResponse<>();
        response.setContent(resultPage.getContent());
        response.setPageNumber(resultPage.getNumber());
        response.setPageSize(resultPage.getSize());
        response.setTotalPages(resultPage.getTotalPages());
        response.setTotalElements(resultPage.getTotalElements());

        return responseService.getPagingResult(response);
    }
}
