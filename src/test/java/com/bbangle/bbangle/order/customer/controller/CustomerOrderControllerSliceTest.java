package com.bbangle.bbangle.order.customer.controller;

import static com.bbangle.bbangle.common.service.ResponseService.CommonResponse.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
import com.bbangle.bbangle.order.customer.controller.dto.request.CreateOrderRequest;
import com.bbangle.bbangle.order.customer.controller.dto.request.CreateOrderRequest.OptionOrderRequest;
import com.bbangle.bbangle.order.customer.controller.dto.request.CreateOrderRequest.OrdererRequest;
import com.bbangle.bbangle.order.customer.controller.dto.request.CreateOrderRequest.PaymentAmountRequest;
import com.bbangle.bbangle.order.customer.controller.dto.request.CreateOrderRequest.ProductOrderRequest;
import com.bbangle.bbangle.order.customer.controller.dto.request.CreateOrderRequest.ShippingAddressRequest;
import com.bbangle.bbangle.order.customer.controller.dto.request.CreateOrderRequest.StoreOrderRequest;
import com.bbangle.bbangle.order.customer.controller.dto.response.CreateOrderResponse;
import com.bbangle.bbangle.order.customer.controller.dto.response.CreateOrderResponse.StoreOrderResponse;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerDeliveryInfo;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerOrderDetail;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerOrderDetailItem;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerPaymentSummary;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderInfo;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderItemInfo;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderPageResponse;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderProgress;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderStatusCounts;
import com.bbangle.bbangle.order.customer.service.CustomerOrderCreateService;
import com.bbangle.bbangle.order.customer.service.CustomerOrderService;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderDetailCommand;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderSearchCommand;
import com.bbangle.bbangle.order.domain.model.CustomerOrderCategory;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.payment.domain.PaymentMethod;
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

    @Autowired
    private JsonDataEncoder jsonDataEncoder;

    @SpyBean
    private ResponseService responseService;

    @SpyBean
    private GlobalControllerAdvice globalControllerAdvice;

    @MockBean
    private CustomerOrderService customerOrderService;

    @MockBean
    private CustomerOrderCreateService customerOrderCreateService;

    private static final String TRANSACTION_ID = "550e8400-e29b-41d4-a716-446655440000";

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
            OrderStatus.SHIPPED, "배송완료", "CJ대한통운", "123-456", progress);
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
            List.of("고단백", "글루텐프리"), true, "CJ대한통운", "123-456-789", progress);
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

    // ====================== 주문 생성 ======================

    private static CreateOrderRequest createOrderRequest(
        List<StoreOrderRequest> stores, boolean privacyAgreed, String ordererPhone
    ) {
        return new CreateOrderRequest(
            stores,
            new OrdererRequest("홍길동", ordererPhone, "buyer@example.com"),
            new ShippingAddressRequest(
                "홍길동", "01012345678", "13529",
                "경기도 성남시 분당구 판교역로 166", "101동 1001호", "문 앞에 놔주세요", true),
            PaymentMethod.CARD,
            privacyAgreed,
            new PaymentAmountRequest(24_000, 0, 3_000, 27_000));
    }

    private static StoreOrderRequest storeRequest(OptionOrderRequest option) {
        return new StoreOrderRequest(1L, 3_000, List.of(new ProductOrderRequest(10L, List.of(option))));
    }

    private static CreateOrderRequest validOrderRequest() {
        return createOrderRequest(
            List.of(storeRequest(new OptionOrderRequest(101L, 2, 12_000))), true, "01012345678");
    }

    @DisplayName("주문 생성 API - 성공(스토어별 주문이 결제 1건으로 묶여 응답된다)")
    @Test
    void givenValidRequest_whenCreateOrder_thenReturnsPaymentAndStoreOrders() throws Exception {
        // given
        CreateOrderResponse mockResponse = new CreateOrderResponse(
            "PAY-20260924-7K3XQ2M9",
            "글루텐프리 케이크 외 1건",
            33_000L,
            List.of(
                new StoreOrderResponse(11L, "ORDER-20260924-A1B2C3D4", 1L, "빵그리의 오븐",
                    24_000L, 0L, 3_000L, 27_000L),
                new StoreOrderResponse(12L, "ORDER-20260924-E5F6G7H8", 2L, "저당공방",
                    6_000L, 0L, 0L, 6_000L)));

        given(customerOrderCreateService.create(any(CreateOrderCommand.class)))
            .willReturn(mockResponse);

        // when & then
        mvc.perform(post("/api/v1/customer/orders")
                .with(authentication(memberAuth(1L)))
                .header("X-Transaction-Id", TRANSACTION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(validOrderRequest())))

            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.result.paymentNumber").value("PAY-20260924-7K3XQ2M9"))
            .andExpect(jsonPath("$.result.orderName").value("글루텐프리 케이크 외 1건"))
            .andExpect(jsonPath("$.result.totalAmount").value(33000))
            .andExpect(jsonPath("$.result.orders.length()").value(2))
            .andExpect(jsonPath("$.result.orders[0].orderNumber").value("ORDER-20260924-A1B2C3D4"))
            .andExpect(jsonPath("$.result.orders[0].deliveryFee").value(3000))
            .andExpect(jsonPath("$.result.orders[1].deliveryFee").value(0));

        then(customerOrderCreateService).should(times(1)).create(any(CreateOrderCommand.class));
    }

    @DisplayName("주문 생성 API - 실패(개인정보 수집에 동의하지 않으면 주문할 수 없다)")
    @Test
    void givenPrivacyNotAgreed_whenCreateOrder_thenReturns400() throws Exception {
        // given
        CreateOrderRequest request = createOrderRequest(
            List.of(storeRequest(new OptionOrderRequest(101L, 1, 12_000))), false, "01012345678");

        // when & then
        mvc.perform(post("/api/v1/customer/orders")
                .with(authentication(memberAuth(1L)))
                .header("X-Transaction-Id", TRANSACTION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(request)))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        then(customerOrderCreateService).shouldHaveNoInteractions();
    }

    @DisplayName("주문 생성 API - 실패(스토어 목록이 비어 있으면 주문할 수 없다)")
    @Test
    void givenEmptyStores_whenCreateOrder_thenReturns400() throws Exception {
        // given
        CreateOrderRequest request = createOrderRequest(List.of(), true, "01012345678");

        // when & then
        mvc.perform(post("/api/v1/customer/orders")
                .with(authentication(memberAuth(1L)))
                .header("X-Transaction-Id", TRANSACTION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(request)))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        then(customerOrderCreateService).shouldHaveNoInteractions();
    }

    @DisplayName("주문 생성 API - 실패(수량이 1개 미만이면 주문할 수 없다)")
    @Test
    void givenNonPositiveQuantity_whenCreateOrder_thenReturns400() throws Exception {
        // given
        CreateOrderRequest request = createOrderRequest(
            List.of(storeRequest(new OptionOrderRequest(101L, 0, 12_000))), true, "01012345678");

        // when & then
        mvc.perform(post("/api/v1/customer/orders")
                .with(authentication(memberAuth(1L)))
                .header("X-Transaction-Id", TRANSACTION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(request)))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        then(customerOrderCreateService).shouldHaveNoInteractions();
    }

    @DisplayName("주문 생성 API - 실패(연락처에 숫자가 아닌 값이 있으면 주문할 수 없다)")
    @Test
    void givenNonNumericPhone_whenCreateOrder_thenReturns400() throws Exception {
        // given
        CreateOrderRequest request = createOrderRequest(
            List.of(storeRequest(new OptionOrderRequest(101L, 1, 12_000))), true, "010-1234-5678");

        // when & then
        mvc.perform(post("/api/v1/customer/orders")
                .with(authentication(memberAuth(1L)))
                .header("X-Transaction-Id", TRANSACTION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(request)))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));

        then(customerOrderCreateService).shouldHaveNoInteractions();
    }

    @DisplayName("주문 생성 API - 실패(서버 계산 금액과 다르면 주문이 거부된다)")
    @Test
    void givenTamperedAmount_whenCreateOrder_thenReturnsErrorCode() throws Exception {
        // given
        given(customerOrderCreateService.create(any(CreateOrderCommand.class)))
            .willThrow(new BbangleException(BbangleErrorCode.ORDER_AMOUNT_MISMATCH));

        // when & then
        mvc.perform(post("/api/v1/customer/orders")
                .with(authentication(memberAuth(1L)))
                .header("X-Transaction-Id", TRANSACTION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(validOrderRequest())))

            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value(BbangleErrorCode.ORDER_AMOUNT_MISMATCH.getCode()))
            .andExpect(jsonPath("$.message").value(BbangleErrorCode.ORDER_AMOUNT_MISMATCH.getMessage()));

        then(globalControllerAdvice).should(times(1))
            .handleBbangleException(any(), any(BbangleException.class));
    }

    @DisplayName("주문 생성 API - 실패(X-Transaction-Id 헤더가 없으면 주문할 수 없다)")
    @Test
    void givenNoTransactionIdHeader_whenCreateOrder_thenReturns4xx() throws Exception {
        // when & then : 중복 요청 방지 헤더는 필수다
        mvc.perform(post("/api/v1/customer/orders")
                .with(authentication(memberAuth(1L)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(validOrderRequest())))

            .andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.success").value(false));

        then(customerOrderCreateService).shouldHaveNoInteractions();
    }
}
