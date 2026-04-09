package com.bbangle.bbangle.order.seller.controller;

import static com.bbangle.bbangle.common.service.ResponseService.CommonResponse.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.exception.GlobalControllerAdvice;
import com.bbangle.bbangle.order.seller.controller.dto.response.CompletedOrderResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.CompletedOrderResponse.CompletedOrderPageResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.CompletedOrderResponse.CompletedOrderStatusCounts;
import com.bbangle.bbangle.order.seller.service.SellerOrderService;
import com.bbangle.bbangle.claim.seller.service.SellerReturnService;
import com.bbangle.bbangle.claim.seller.service.SellerExchangeService;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.CompletedOrderSearchCommand;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@DisplayName("[단위테스트] SellerOrderController - 완료 주문 내역")
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@WebMvcTest(controllers = SellerOrderController.class)
class SellerOrderCompletedControllerSliceTest {

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private ResponseService responseService;

    @SpyBean
    private GlobalControllerAdvice globalControllerAdvice;

    @MockBean
    private SellerOrderService sellerOrderService;

    @MockBean
    private SellerReturnService sellerReturnService;

    @MockBean
    private SellerExchangeService sellerExchangeService;

    @DisplayName("완료 주문 내역 조회 - 검색 결과가 없는 경우 (엣지 케이스)")
    @Test
    void getCompletedOrders_ReturnsEmpty_WhenNoOrdersFound() throws Exception {
        // Given
        Long sellerId = 1L;
        CompletedOrderPageResponse mockResponse = new CompletedOrderPageResponse(
            new BbanglePageResponse<>(Collections.emptyList(), 0, 10, 0, 0L),
            CompletedOrderStatusCounts.of(0, 0, 0, 0)
        );

        given(sellerOrderService.getCompletedOrders(any(CompletedOrderSearchCommand.class)))
            .willReturn(mockResponse);

        // When & Then
        mvc.perform(get("/api/v1/seller/orders/completed")
                .with(authentication(new UsernamePasswordAuthenticationToken(
                    sellerId, "N/A", List.of(new SimpleGrantedAuthority("ROLE_SELLER")))))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.result.orders.content").isEmpty())
            .andExpect(jsonPath("$.result.statusCounts.total").value(0));

        then(sellerOrderService).should(times(1)).getCompletedOrders(any());
    }

    @DisplayName("완료 주문 내역 조회 - 모든 필터가 null인 경우 (엣지 케이스)")
    @Test
    void getCompletedOrders_HandlesNullFilters_Successfully() throws Exception {
        // Given
        Long sellerId = 1L;
        CompletedOrderPageResponse mockResponse = new CompletedOrderPageResponse(
            new BbanglePageResponse<>(Collections.emptyList(), 0, 10, 0, 0L),
            CompletedOrderStatusCounts.of(0, 0, 0, 0)
        );

        given(sellerOrderService.getCompletedOrders(any(CompletedOrderSearchCommand.class)))
            .willReturn(mockResponse);

        // When & Then
        mvc.perform(get("/api/v1/seller/orders/completed")
                .with(authentication(new UsernamePasswordAuthenticationToken(
                    sellerId, "N/A", List.of(new SimpleGrantedAuthority("ROLE_SELLER")))))
                .contentType(MediaType.APPLICATION_JSON)) // 파라미터 전혀 없이 호출
            .andExpect(status().isOk());

        then(sellerOrderService).should(times(1)).getCompletedOrders(any());
    }

    @DisplayName("완료 주문 내역 조회 - 서비스 내부 에러 발생 시 (엣지 케이스)")
    @Test
    void getCompletedOrders_ThrowsException_WhenServiceFails() throws Exception {
        // Given
        Long sellerId = 1L;
        given(sellerOrderService.getCompletedOrders(any(CompletedOrderSearchCommand.class)))
            .willThrow(new BbangleException(BbangleErrorCode.INTERNAL_SERVER_ERROR));

        // When & Then
        mvc.perform(get("/api/v1/seller/orders/completed")
                .with(authentication(new UsernamePasswordAuthenticationToken(
                    sellerId, "N/A", List.of(new SimpleGrantedAuthority("ROLE_SELLER")))))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is5xxServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(BbangleErrorCode.INTERNAL_SERVER_ERROR.getCode()));

        then(sellerOrderService).should(times(1)).getCompletedOrders(any());
        then(globalControllerAdvice).should(times(1))
            .handleBbangleException(any(HttpServletRequest.class), any(BbangleException.class));
        then(responseService).should(times(1)).getFailResult(anyString(), anyInt());
    }

}
