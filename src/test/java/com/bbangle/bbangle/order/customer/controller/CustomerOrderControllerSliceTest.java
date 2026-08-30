package com.bbangle.bbangle.order.customer.controller;

import static com.bbangle.bbangle.common.service.ResponseService.CommonResponse.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerDeliveryInfo;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerOrderDetail;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerOrderDetailItem;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerPaymentSummary;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderInfo;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderItemInfo;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderPageResponse;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderProgress;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderStatusCounts;
import com.bbangle.bbangle.order.customer.service.CustomerOrderService;
import com.bbangle.bbangle.order.customer.service.PurchaseConfirmService;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderDetailCommand;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderSearchCommand;
import com.bbangle.bbangle.order.domain.model.CustomerOrderCategory;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@DisplayName("[컨트롤러] CustomerOrderController")
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@WebMvcTest(controllers = CustomerOrderController.class)
class CustomerOrderControllerSliceTest {

    @Autowired
    private MockMvc mvc;

    @SpyBean
    private ResponseService responseService;

    @SpyBean
    private GlobalControllerAdvice globalControllerAdvice;

    @MockBean
    private CustomerOrderService customerOrderService;

    @MockBean
    private PurchaseConfirmService purchaseConfirmService;

    private static UsernamePasswordAuthenticationToken memberAuth(Long memberId) {
        return new UsernamePasswordAuthenticationToken(
            memberId, "N/A", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    @DisplayName("주문목록 조회 API - 성공(데이터 있음)")
    @Test
    void givenAuthenticatedMember_whenGetOrders_thenReturnsOrders() throws Exception {
        // given
        Long memberId = 1L;

        CustomerOrderProgress progress = CustomerOrderProgress.of(
            CustomerOrderCategory.NORMAL, "배송완료", 3,
            List.of("결제완료", "상품제작중", "상품발송", "배송완료", "구매확정"));
        CustomerOrderItemInfo item = new CustomerOrderItemInfo(
            10L, "저당 베이글 세트", "저칼로리 베이글", 2, 4700L, 9400L,
            OrderStatus.SHIPPED, "배송완료", "CJ대한통운", "123-456", progress, true, false);
        CustomerOrderInfo orderInfo = new CustomerOrderInfo(
            1L, "ORDER-2025-06-14-00001", LocalDate.of(2025, 6, 14), 9400L, null, List.of(item));

        CustomerOrderPageResponse mockResponse = new CustomerOrderPageResponse(
            new BbanglePageResponse<>(List.of(orderInfo), 0, 10, 1, 1L),
            CustomerOrderStatusCounts.of(1L, 0L, 0L, 0L, 0L));

        given(customerOrderService.getOrders(any(CustomerOrderSearchCommand.class)))
            .willReturn(mockResponse);

        // when & then
        mvc.perform(get("/api/v1/customer/orders")
                .with(authentication(memberAuth(memberId)))
                .param("page", "0")
                .param("size", "10")
                .param("sort", "orderDate,desc"))

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.result.orders.content").isArray())
            .andExpect(jsonPath("$.result.orders.content.length()").value(1))
            .andExpect(jsonPath("$.result.orders.content[0].orderNumber").value("ORDER-2025-06-14-00001"))
            .andExpect(jsonPath("$.result.orders.content[0].orderDate").value("2025-06-14"))
            .andExpect(jsonPath("$.result.orders.content[0].orderItems[0].progress.currentStep").value("배송완료"))
            .andExpect(jsonPath("$.result.orders.content[0].orderItems[0].progress.currentStepIndex").value(3))
            .andExpect(jsonPath("$.result.statusCounts.total").value(1))
            .andExpect(jsonPath("$.result.statusCounts.inProgress").value(1));

        then(customerOrderService).should(times(1)).getOrders(any(CustomerOrderSearchCommand.class));
        then(responseService).should(times(1)).getSingleResult(mockResponse);
    }

    @DisplayName("주문목록 조회 API - 성공(빈 결과)")
    @Test
    void givenNoOrders_whenGetOrders_thenReturnsEmptyPage() throws Exception {
        // given
        CustomerOrderPageResponse mockResponse = new CustomerOrderPageResponse(
            new BbanglePageResponse<>(Collections.emptyList(), 0, 10, 0, 0L),
            CustomerOrderStatusCounts.of(0L, 0L, 0L, 0L, 0L));

        given(customerOrderService.getOrders(any(CustomerOrderSearchCommand.class)))
            .willReturn(mockResponse);

        // when & then
        mvc.perform(get("/api/v1/customer/orders")
                .with(authentication(memberAuth(1L))))

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.orders.content").isArray())
            .andExpect(jsonPath("$.result.orders.totalElements").value(0))
            .andExpect(jsonPath("$.result.statusCounts.total").value(0));

        then(customerOrderService).should(times(1)).getOrders(any(CustomerOrderSearchCommand.class));
    }

    @DisplayName("주문목록 조회 API - 실패(존재하지 않는 회원)")
    @Test
    void givenNonExistingMember_whenGetOrders_thenReturns4xx() throws Exception {
        // given
        given(customerOrderService.getOrders(any(CustomerOrderSearchCommand.class)))
            .willThrow(new BbangleException(BbangleErrorCode.CUSTOMER_ORDER_MEMBER_NOT_FOUND));

        // when & then
        mvc.perform(get("/api/v1/customer/orders")
                .with(authentication(memberAuth(999L))))

            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(BbangleErrorCode.CUSTOMER_ORDER_MEMBER_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(BbangleErrorCode.CUSTOMER_ORDER_MEMBER_NOT_FOUND.getMessage()));

        then(globalControllerAdvice).should(times(1))
            .handleBbangleException(any(), any(BbangleException.class));
    }

    @DisplayName("주문 상세 조회 API - 성공")
    @Test
    void givenAuthenticatedMember_whenGetOrderDetail_thenReturnsDetail() throws Exception {
        // given
        Long memberId = 1L;
        Long orderId = 5L;

        CustomerOrderProgress progress = CustomerOrderProgress.of(
            CustomerOrderCategory.NORMAL, "상품발송", 2,
            List.of("결제완료", "상품제작중", "상품발송", "배송완료", "구매확정"));
        CustomerOrderDetailItem item = new CustomerOrderDetailItem(
            10L, "비건빵빵이네", "저당 베이글 세트", "저칼로리 베이글", "상품발송",
            OrderStatus.SHIPPED, 5800L, 4700L, 19, 2, 9400L,
            List.of("고단백", "글루텐프리"), true, "CJ대한통운", "123-456-789", progress, false, false);
        CustomerPaymentSummary payment = new CustomerPaymentSummary(
            11600L, 2200L, 2500L, 11900L, 2200L, "총 2,200원 할인 받았어요", true, null);
        CustomerDeliveryInfo delivery = new CustomerDeliveryInfo(
            "홍길동", "서울특별시 강남구 테헤란로 123 101동 1001호", "06234",
            "010-1234-5678", "부재 시 경비실에 맡겨주세요", true);
        CustomerOrderDetail mockResponse = new CustomerOrderDetail(
            orderId, "ORDER-2025-05-13-00001", LocalDate.of(2025, 5, 13), payment, delivery, List.of(item));

        given(customerOrderService.getOrderDetail(any(CustomerOrderDetailCommand.class)))
            .willReturn(mockResponse);

        // when & then
        mvc.perform(get("/api/v1/customer/orders/{orderId}", orderId)
                .with(authentication(memberAuth(memberId))))

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.result.orderNumber").value("ORDER-2025-05-13-00001"))
            .andExpect(jsonPath("$.result.orderDate").value("2025-05-13"))
            .andExpect(jsonPath("$.result.payment.finalPaymentAmount").value(11900))
            .andExpect(jsonPath("$.result.payment.totalDiscountMessage").value("총 2,200원 할인 받았어요"))
            .andExpect(jsonPath("$.result.payment.receiptViewable").value(true))
            .andExpect(jsonPath("$.result.delivery.addressChangeable").value(true))
            .andExpect(jsonPath("$.result.orderItems[0].storeName").value("비건빵빵이네"))
            .andExpect(jsonPath("$.result.orderItems[0].statusBadge").value("상품발송"))
            .andExpect(jsonPath("$.result.orderItems[0].discountRate").value(19))
            .andExpect(jsonPath("$.result.orderItems[0].deliveryTrackable").value(true));

        then(customerOrderService).should(times(1)).getOrderDetail(any(CustomerOrderDetailCommand.class));
    }

    @DisplayName("주문 상세 조회 API - 실패(존재하지 않거나 접근 불가 주문)")
    @Test
    void givenInaccessibleOrder_whenGetOrderDetail_thenReturns4xx() throws Exception {
        // given
        given(customerOrderService.getOrderDetail(any(CustomerOrderDetailCommand.class)))
            .willThrow(new BbangleException(BbangleErrorCode.CUSTOMER_ORDER_NOT_FOUND));

        // when & then
        mvc.perform(get("/api/v1/customer/orders/{orderId}", 999L)
                .with(authentication(memberAuth(1L))))

            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(BbangleErrorCode.CUSTOMER_ORDER_NOT_FOUND.getCode()));

        then(globalControllerAdvice).should(times(1))
            .handleBbangleException(any(), any(BbangleException.class));
    }

    @DisplayName("구매확정 API - 성공")
    @Test
    void givenDeliveredOrderItem_whenConfirmPurchase_thenReturnsConfirmedId() throws Exception {
        // given
        Long memberId = 1L;
        Long orderId = 100L;
        Long orderItemId = 10L;

        given(purchaseConfirmService.confirm(memberId, orderId, orderItemId)).willReturn(orderItemId);

        // when & then
        mvc.perform(post("/api/v1/customer/orders/{orderId}/items/{orderItemId}/confirm", orderId, orderItemId)
                .with(authentication(memberAuth(memberId)))
                .with(csrf()))

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.result").value(orderItemId));

        then(purchaseConfirmService).should(times(1)).confirm(memberId, orderId, orderItemId);
    }

    @DisplayName("구매확정 API - 실패(배송완료 전 구매확정 시도)")
    @Test
    void givenNotDeliveredOrderItem_whenConfirmPurchase_thenReturns4xx() throws Exception {
        // given
        given(purchaseConfirmService.confirm(any(), any(), any()))
            .willThrow(new BbangleException(BbangleErrorCode.PURCHASE_CONFIRM_NOT_ALLOWED));

        // when & then
        mvc.perform(post("/api/v1/customer/orders/{orderId}/items/{orderItemId}/confirm", 100L, 10L)
                .with(authentication(memberAuth(1L)))
                .with(csrf()))

            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(BbangleErrorCode.PURCHASE_CONFIRM_NOT_ALLOWED.getCode()));

        then(globalControllerAdvice).should(times(1))
            .handleBbangleException(any(), any(BbangleException.class));
    }
}
