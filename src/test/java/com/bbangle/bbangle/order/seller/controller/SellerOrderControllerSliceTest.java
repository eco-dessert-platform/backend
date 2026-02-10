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
import com.bbangle.bbangle.common.dto.SearchFormDto.DefaultSearchCondition;
import com.bbangle.bbangle.common.dto.SearchFormDto.DefaultSortDirectionCondition;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.exception.GlobalControllerAdvice;
import com.bbangle.bbangle.order.domain.model.CompletedOrderSearchType;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.seller.controller.dto.request.OrderRequest.OrderSearchRequest;
import com.bbangle.bbangle.order.seller.controller.dto.request.SellerOrderRequest.OrderConfirmRequest;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderSearchResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.seller.service.SellerOrderService;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
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

                // 부분 성공 정책 예시: 100, 102만 확정됨
                OrderConfirmResponse serviceResponse = OrderConfirmResponse.of(orderId, List.of(100L, 102L));

                given(sellerOrderService.confirmOrder(any())).willReturn(serviceResponse);

                // When & Then
                mvc.perform(post("/api/v1/seller/orders/{orderId}/confirm", orderId)
                                .with(authentication(new UsernamePasswordAuthenticationToken(
                                                1L,
                                                "N/A",
                                                List.of(new SimpleGrantedAuthority("ROLE_SELLER")))))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonDataEncoder.encode(request)))

                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
                                .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
                                .andExpect(jsonPath("$.result.orderId").value(orderId))
                                .andExpect(jsonPath("$.result.confirmedOrderItemIds[0]").value(100))
                                .andExpect(jsonPath("$.result.confirmedOrderItemIds[1]").value(102));

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
                                                List.of(new SimpleGrantedAuthority("ROLE_SELLER")))))
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
                OrderConfirmRequest request = new OrderConfirmRequest(List.of()); // @NotEmpty 위반

                // When & Then
                mvc.perform(post("/api/v1/seller/orders/{orderId}/confirm", orderId)
                                .with(user("seller").roles("SELLER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonDataEncoder.encode(request)))
                                .andExpect(status().is4xxClientError());

                // validation에서 막히면 service 호출 안 되는 게 정상
                then(sellerOrderService).shouldHaveNoInteractions();
        }

        @DisplayName("주문 검색 API - 성공")
        @Test
        void givenValidSearchRequest_whenSearchOrders_thenReturnsOrderList() throws Exception {
                // Given
                Long sellerId = 1L;

                DefaultSearchCondition searchCondition = new DefaultSearchCondition();
                searchCondition.setStartDate(LocalDate.of(2025, 1, 1));
                searchCondition.setEndDate(LocalDate.of(2025, 1, 31));
                searchCondition.setKeywords(Collections.singletonList("홍길동"));
                searchCondition.setIsMultipleSearch(false);

                OrderSearchRequest request = new OrderSearchRequest(
                                OrderDeliveryStatus.PREPARING, CompletedOrderSearchType.ORDER_NUMBER,
                                searchCondition);

                BbanglePageResponse<OrderSearchResponse> mockResponse = new BbanglePageResponse<>(
                                Collections.emptyList(),
                                0,
                                20,
                                0,
                                0L);

                given(sellerOrderService.orderSearch(any(OrderSearchCommand.class)))
                                .willReturn(mockResponse);

                // When & Then
                mvc.perform(post("/api/v1/seller/orders/orders")
                                .with(authentication(new UsernamePasswordAuthenticationToken(
                                                sellerId,
                                                "N/A",
                                                List.of(new SimpleGrantedAuthority("ROLE_SELLER")))))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonDataEncoder.encode(request))
                                .param("page", "0")
                                .param("size", "20")
                                .param("sort", "orderDate,desc"))

                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
                                .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
                                .andExpect(jsonPath("$.result.content").isArray())
                                .andExpect(jsonPath("$.result.page").value(0))
                                .andExpect(jsonPath("$.result.size").value(20))
                                .andExpect(jsonPath("$.result.totalPages").value(0))
                                .andExpect(jsonPath("$.result.totalElements").value(0));

                then(sellerOrderService).should(times(1)).orderSearch(any(OrderSearchCommand.class));
                then(responseService).should(times(1)).getSingleResult(mockResponse);
        }

        @DisplayName("주문 검색 API - 성공(검색 결과 있음)")
        @Test
        void givenValidSearchRequest_whenSearchOrders_thenReturnsOrderListWithData() throws Exception {
                // Given
                Long sellerId = 1L;

                DefaultSearchCondition searchCondition = new DefaultSearchCondition();
                searchCondition.setStartDate(LocalDate.of(2025, 1, 1));
                searchCondition.setEndDate(LocalDate.of(2025, 1, 31));
                searchCondition.setKeywords(Collections.singletonList("2503020013"));
                searchCondition.setIsMultipleSearch(false);

                OrderSearchRequest request = new OrderSearchRequest(
                                null,
                                CompletedOrderSearchType.ORDER_NUMBER,
                                searchCondition);

                OrderSearchResponse orderSearchResponse = OrderSearchResponse.builder()
                                .orderNumber("2503020013")
                                .recipientName("홍길동")
                                .orderItems(Collections.emptyList())
                                .totalOrderPrice("24400")
                                .build();

                BbanglePageResponse<OrderSearchResponse> mockResponse = new BbanglePageResponse<>(
                                List.of(orderSearchResponse),
                                0,
                                20,
                                1,
                                1L);

                given(sellerOrderService.orderSearch(any(OrderSearchCommand.class)))
                                .willReturn(mockResponse);

                // When & Then
                mvc.perform(post("/api/v1/seller/orders/orders")
                                .with(authentication(new UsernamePasswordAuthenticationToken(
                                                sellerId,
                                                "N/A",
                                                List.of(new SimpleGrantedAuthority("ROLE_SELLER")))))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonDataEncoder.encode(request))
                                .param("page", "0")
                                .param("size", "20")
                                .param("sort", "orderDate,desc"))

                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.result.content").isArray())
                                .andExpect(jsonPath("$.result.content.length()").value(1))
                                .andExpect(jsonPath("$.result.content[0].orderNumber").value("2503020013"))
                                .andExpect(jsonPath("$.result.content[0].recipientName").value("홍길동"))
                                .andExpect(jsonPath("$.result.content[0].totalOrderPrice").value("24400"))
                                .andExpect(jsonPath("$.result.totalElements").value(1));

                then(sellerOrderService).should(times(1)).orderSearch(any(OrderSearchCommand.class));
        }

}
