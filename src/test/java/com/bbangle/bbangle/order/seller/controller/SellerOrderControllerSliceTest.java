package com.bbangle.bbangle.order.seller.controller;

import static com.bbangle.bbangle.common.service.ResponseService.CommonResponse.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.exception.GlobalControllerAdvice;
import com.bbangle.bbangle.order.seller.controller.dto.request.SellerOrderRequest.OrderConfirmRequest;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.seller.service.SellerOrderService;
import jakarta.servlet.http.HttpServletRequest;
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
@DisplayName("[컨트롤러] SellerOrderController")
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@WebMvcTest(controllers = SellerOrderController.class)
class SellerOrderControllerSliceTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JsonDataEncoder jsonDataEncoder;

    @SpyBean
    private ResponseService responseService;

    @SpyBean
    private GlobalControllerAdvice globalControllerAdvice;

    @MockBean
    private SellerOrderService sellerOrderService;

    @DisplayName("발주 확인 API - 성공")
    @Test
    void givenValidRequest_whenConfirmOrder_thenReturnsSuccess() throws Exception {
        // Given
        Long sellerId = 1L;
        Long orderId = 10L;

        OrderConfirmRequest request = new OrderConfirmRequest(List.of(100L, 101L, 102L));

        // 부분 성공 정책 예시: 100, 102만 확정됨 (101은 실패)
        SellerOrderResponse.Summary summary = SellerOrderResponse.Summary.of(3, 2, 1);
        SellerOrderResponse.Content content = SellerOrderResponse.Content.of(
            orderId,
            summary,
            List.of(100L, 102L),  // confirmedOrderItemIds
            List.of(101L)          // failedOrderItemIds
        );
        OrderConfirmResponse serviceResponse = OrderConfirmResponse.of(content);

        given(sellerOrderService.confirmOrder(any())).willReturn(serviceResponse);

        // When & Then
        mvc.perform(post("/api/v1/seller/orders/{orderId}/confirm", orderId)
                .with(authentication(new UsernamePasswordAuthenticationToken(
                    1L,
                    "N/A",
                    List.of(new SimpleGrantedAuthority("ROLE_SELLER"))
                )))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(request)))

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
            .andExpect(jsonPath("$.result.content.orderId").value(orderId))
            .andExpect(jsonPath("$.result.content.summary.requestedCount").value(3))
            .andExpect(jsonPath("$.result.content.summary.successCount").value(2))
            .andExpect(jsonPath("$.result.content.summary.failCount").value(1))
            .andExpect(jsonPath("$.result.content.confirmedOrderItemIds[0]").value(100))
            .andExpect(jsonPath("$.result.content.confirmedOrderItemIds[1]").value(102))
            .andExpect(jsonPath("$.result.content.failedOrderItemIds[0]").value(101));

        then(sellerOrderService).should(times(1)).confirmOrder(any());
        then(responseService).should(times(1)).getSingleResult(serviceResponse);
    }

    @DisplayName("발주 확인 API - 실패(주문 없음)")
    @Test
    void givenNonExistingOrder_whenConfirmOrder_thenReturns4xx() throws Exception {
        // Given
        Long orderId = -1L;
        OrderConfirmRequest request = new OrderConfirmRequest(List.of(100L));

        given(sellerOrderService.confirmOrder(any()))
            .willThrow(new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));

        // When & Then
        mvc.perform(post("/api/v1/seller/orders/{orderId}/confirm", orderId)
                .with(authentication(new UsernamePasswordAuthenticationToken(
                    1L,
                    "N/A",
                    List.of(new SimpleGrantedAuthority("ROLE_SELLER"))
                )))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(request)))

            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(BbangleErrorCode.ORDER_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(BbangleErrorCode.ORDER_NOT_FOUND.getMessage()));

        then(sellerOrderService).should(times(1)).confirmOrder(any());
        then(globalControllerAdvice).should(times(1))
            .handleBbangleException(any(HttpServletRequest.class), any(BbangleException.class));
        then(responseService).should(times(1)).getFailResult(anyString(), anyInt());
    }

    @DisplayName("발주 확인 API - 실패(orderItemIds 비어있음 -> validation 400)")
    @Test
    void givenEmptyOrderItemIds_whenConfirmOrder_thenReturns400() throws Exception {
        // Given
        Long orderId = 10L;
        OrderConfirmRequest request = new OrderConfirmRequest(List.of());

        // When & Then
        mvc.perform(post("/api/v1/seller/orders/{orderId}/confirm", orderId)
                .with(user("seller").roles("SELLER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(request)))
            .andExpect(status().is4xxClientError());

        // validation에서 막히면 service 호출 안 되는 게 정상
        then(sellerOrderService).shouldHaveNoInteractions();
    }
}
