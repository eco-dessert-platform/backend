package com.bbangle.bbangle.order.customer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.CustomerApiPath;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.customer.controller.dto.response.OrderReceiptResponse;
import com.bbangle.bbangle.order.customer.service.OrderReceiptService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("[컨트롤러 테스트] OrderReceiptController")
@Import({
    TestSlackAdaptorConfig.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@WebMvcTest(OrderReceiptController.class)
@ActiveProfiles("test")
class OrderReceiptControllerTest {

    private static final String RECEIPT_URL = CustomerApiPath.PREFIX + "/orders/{orderId}/receipt";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OrderReceiptService orderReceiptService;

    @SpyBean
    private ResponseService responseService;

    @Nested
    @DisplayName("GET /api/v1/customer/orders/{orderId}/receipt")
    class GetReceipt {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @WithMockUser(roles = "CUSTOMER")
            @DisplayName("정상 요청 시 영수증 정보를 반환한다")
            void getReceipt_success() throws Exception {
                // given
                Long orderId = 1L;
                OrderReceiptResponse response = new OrderReceiptResponse(
                    new OrderReceiptResponse.PaymentInfo(
                        "1232241511", "신용/체크카드", "신한",
                        "484322****1234", "일시불",
                        LocalDateTime.of(2025, 5, 29, 18, 0, 10)
                    ),
                    new OrderReceiptResponse.PurchaseInfo(
                        "20100024120", "비건 쌀빵 1종 외 1건",
                        6727, 0, 673, 7400
                    ),
                    new OrderReceiptResponse.StoreInfo(
                        "빵그리의 오븐", "521-03-12345", "경기도 성남시 중원구 성남대로 1234 101호"
                    )
                );

                given(orderReceiptService.getReceipt(any(), eq(orderId))).willReturn(response);

                // when & then
                mvc.perform(get(RECEIPT_URL, orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.result.paymentInfo.approvalNumber").value("1232241511"))
                    .andExpect(jsonPath("$.result.paymentInfo.transactionType").value("신용/체크카드"))
                    .andExpect(jsonPath("$.result.paymentInfo.cardType").value("신한"))
                    .andExpect(jsonPath("$.result.paymentInfo.cardNumber").value("484322****1234"))
                    .andExpect(jsonPath("$.result.paymentInfo.installment").value("일시불"))
                    .andExpect(jsonPath("$.result.purchaseInfo.orderNumber").value("20100024120"))
                    .andExpect(jsonPath("$.result.purchaseInfo.productName").value("비건 쌀빵 1종 외 1건"))
                    .andExpect(jsonPath("$.result.purchaseInfo.totalAmount").value(7400))
                    .andExpect(jsonPath("$.result.purchaseInfo.taxableAmount").value(6727))
                    .andExpect(jsonPath("$.result.purchaseInfo.vat").value(673))
                    .andExpect(jsonPath("$.result.purchaseInfo.nonTaxableAmount").value(0))
                    .andExpect(jsonPath("$.result.storeInfo.storeName").value("빵그리의 오븐"))
                    .andExpect(jsonPath("$.result.storeInfo.businessNumber").value("521-03-12345"));

                then(orderReceiptService).should().getReceipt(any(), eq(orderId));
            }

            @Test
            @WithMockUser(roles = "CUSTOMER")
            @DisplayName("현금 결제 주문은 카드 관련 필드가 null로 반환된다")
            void getReceipt_cashPayment_cardFieldsAreNull() throws Exception {
                // given
                Long orderId = 2L;
                OrderReceiptResponse response = new OrderReceiptResponse(
                    new OrderReceiptResponse.PaymentInfo(
                        "9988776655", "무통장입금", null, null, null,
                        LocalDateTime.of(2025, 5, 29, 18, 0, 10)
                    ),
                    new OrderReceiptResponse.PurchaseInfo(
                        "20100024121", "비건 쌀빵 1종", 9091, 0, 909, 10000
                    ),
                    new OrderReceiptResponse.StoreInfo("빵그리의 오븐", "521-03-12345", "경기도 성남시")
                );

                given(orderReceiptService.getReceipt(any(), eq(orderId))).willReturn(response);

                // when & then
                mvc.perform(get(RECEIPT_URL, orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.paymentInfo.transactionType").value("무통장입금"))
                    .andExpect(jsonPath("$.result.paymentInfo.cardType").isEmpty())
                    .andExpect(jsonPath("$.result.paymentInfo.cardNumber").isEmpty())
                    .andExpect(jsonPath("$.result.paymentInfo.installment").isEmpty());

                then(orderReceiptService).should().getReceipt(any(), eq(orderId));
            }
        }

        @Nested
        @DisplayName("실패")
        class Failure {

            @Test
            @DisplayName("인증 없이 요청하면 401을 반환한다")
            void getReceipt_fail_whenUnauthorized() throws Exception {
                // when & then
                mvc.perform(get(RECEIPT_URL, 1L))
                    .andExpect(status().isUnauthorized());

                then(orderReceiptService).shouldHaveNoInteractions();
                then(responseService).shouldHaveNoInteractions();
            }

            @Test
            @WithMockUser(roles = "CUSTOMER")
            @DisplayName("존재하지 않는 주문 조회 시 404를 반환한다")
            void getReceipt_fail_whenOrderNotFound() throws Exception {
                // given
                Long orderId = 999L;
                given(orderReceiptService.getReceipt(any(), eq(orderId)))
                    .willThrow(new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));

                // when & then
                mvc.perform(get(RECEIPT_URL, orderId))
                    .andExpect(status().isNotFound());

                then(orderReceiptService).should().getReceipt(any(), eq(orderId));
            }

            @Test
            @WithMockUser(roles = "CUSTOMER")
            @DisplayName("다른 회원의 주문 조회 시 403을 반환한다")
            void getReceipt_fail_whenAccessDenied() throws Exception {
                // given
                Long orderId = 1L;
                given(orderReceiptService.getReceipt(any(), eq(orderId)))
                    .willThrow(new BbangleException(BbangleErrorCode.ORDER_ACCESS_DENIED));

                // when & then
                mvc.perform(get(RECEIPT_URL, orderId))
                    .andExpect(status().isForbidden());

                then(orderReceiptService).should().getReceipt(any(), eq(orderId));
            }

            @Test
            @WithMockUser(roles = "SELLER")
            @DisplayName("SELLER 권한으로 요청하면 403을 반환한다")
            void getReceipt_fail_whenSellerRole() throws Exception {
                // when & then
                mvc.perform(get(RECEIPT_URL, 1L))
                    .andExpect(status().isForbidden());

                then(orderReceiptService).shouldHaveNoInteractions();
            }
        }
    }
}
