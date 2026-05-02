package com.bbangle.bbangle.charge.seller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.charge.domain.enums.ChargeCategory;
import com.bbangle.bbangle.charge.domain.enums.ChargeTransactionStatus;
import com.bbangle.bbangle.charge.seller.controller.dto.request.WithdrawalRequest;
import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse;
import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse.ChargeTransactionResponse;
import com.bbangle.bbangle.charge.seller.service.SellerChargeService;
import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("[컨트롤러 테스트] SellerChargeController")
@WebMvcTest(controllers = SellerChargeController.class)
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@ActiveProfiles("test")
class SellerChargeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SellerChargeService chargeService;

    @Autowired
    private JsonDataEncoder jsonDataEncoder;

    private static final Long TEST_SELLER_ID = 1L;
    private static final String BASE_URL = SellerApiPath.PREFIX + "/charge-balance";
    private static final String WITHDRAWAL_URL = BASE_URL + "/withdrawal";
    private static final String TEST_TRANSACTION_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Nested
    @DisplayName("충전금 현황 조회")
    class GetChargeBalance {

        @Test
        @DisplayName("정상 조회 - 기본값으로 조회")
        @WithMockUser(roles = "SELLER")
        void testGetChargeBalanceSuccess() throws Exception {
            // Given
            LocalDate today = LocalDate.now();
            BigDecimal balance = new BigDecimal("100000");
            List<ChargeTransactionResponse> transactions = List.of(
                new ChargeTransactionResponse(
                    today,
                    "250401A1F7",
                    ChargeCategory.ACCUMULATE,
                    50000L,
                    ChargeTransactionStatus.COMPLETED
                ),
                new ChargeTransactionResponse(
                    today.minusDays(1),
                    "250401A1F6",
                    ChargeCategory.DEDUCT,
                    10000L,
                    ChargeTransactionStatus.COMPLETED
                )
            );

            BbanglePageResponse<ChargeTransactionResponse> pageResponse =
                new BbanglePageResponse<>(transactions, 0, 20, 1, 2L);
            ChargeBalanceResponse response = new ChargeBalanceResponse(balance, pageResponse);

            given(chargeService.getChargeBalance(any(), any(), any(), any(Pageable.class)))
                .willReturn(response);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.result.chargeBalance").value(100000))
                .andExpect(jsonPath("$.result.pageResponse.page").value(0))
                .andExpect(jsonPath("$.result.pageResponse.size").value(20))
                .andExpect(jsonPath("$.result.pageResponse.totalElements").value(2))
                .andExpect(jsonPath("$.result.pageResponse.content").isArray())
                .andExpect(jsonPath("$.result.pageResponse.content[0].baseDate").value(today.toString()))
                .andExpect(jsonPath("$.result.pageResponse.content[0].category").value("ACCUMULATE"))
                .andExpect(jsonPath("$.result.pageResponse.content[0].status").value("COMPLETED"));
        }

        @Test
        @DisplayName("날짜 범위 지정 조회")
        @WithMockUser(roles = "SELLER")
        void testGetChargeBalanceWithDateRange() throws Exception {
            // Given
            LocalDate startDate = LocalDate.of(2025, 3, 1);
            LocalDate endDate = LocalDate.of(2025, 3, 7);
            BigDecimal balance = new BigDecimal("250000");

            List<ChargeTransactionResponse> transactions = List.of(
                new ChargeTransactionResponse(
                    endDate,
                    "250401A1F5",
                    ChargeCategory.ACCUMULATE,
                    150000L,
                    ChargeTransactionStatus.COMPLETED
                )
            );

            BbanglePageResponse<ChargeTransactionResponse> pageResponse =
                new BbanglePageResponse<>(transactions, 0, 20, 1, 1L);
            ChargeBalanceResponse response = new ChargeBalanceResponse(balance, pageResponse);

            given(chargeService.getChargeBalance(any(), any(LocalDate.class), any(LocalDate.class),
                any(Pageable.class)))
                .willReturn(response);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.chargeBalance").value(250000))
                .andExpect(jsonPath("$.result.pageResponse.totalElements").value(1));
        }

        @Test
        @DisplayName("페이지네이션 조회")
        @WithMockUser(roles = "SELLER")
        void testGetChargeBalanceWithPagination() throws Exception {
            // Given
            BigDecimal balance = new BigDecimal("500000");
            List<ChargeTransactionResponse> transactions = List.of();

            BbanglePageResponse<ChargeTransactionResponse> pageResponse =
                new BbanglePageResponse<>(transactions, 1, 10, 5, 50L);
            ChargeBalanceResponse response = new ChargeBalanceResponse(balance, pageResponse);

            given(chargeService.getChargeBalance(any(), any(), any(), any(Pageable.class)))
                .willReturn(response);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                    .param("page", "1")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.pageResponse.page").value(1))
                .andExpect(jsonPath("$.result.pageResponse.size").value(10))
                .andExpect(jsonPath("$.result.pageResponse.totalPages").value(5))
                .andExpect(jsonPath("$.result.pageResponse.totalElements").value(50));
        }

        @Test
        @DisplayName("실패 - 충전금 잔액 없음")
        @WithMockUser(roles = "SELLER")
        void testGetChargeBalanceNotFound() throws Exception {
            // Given
            given(chargeService.getChargeBalance(any(), any(), any(), any(Pageable.class)))
                .willThrow(new BbangleException(BbangleErrorCode.CHARGE_BALANCE_NOT_FOUND));

            // When & Then
            mockMvc.perform(get(BASE_URL)
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-821))
                .andExpect(jsonPath("$.message").value("충전금 잔액 정보를 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("인증 없이 접근 시도")
        void testGetChargeBalanceUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get(BASE_URL)
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("다양한 거래 구분 조회 (ACCUMULATE, DEDUCT) - status 함께 검증")
        @WithMockUser(roles = "SELLER")
        void testGetChargeBalanceWithDifferentCategories() throws Exception {
            // Given
            LocalDate today = LocalDate.now();
            BigDecimal balance = new BigDecimal("150000");

            List<ChargeTransactionResponse> transactions = List.of(
                new ChargeTransactionResponse(
                    today,
                    "250401A1F7",
                    ChargeCategory.ACCUMULATE,
                    100000L,
                    ChargeTransactionStatus.COMPLETED
                ),
                new ChargeTransactionResponse(
                    today.minusDays(1),
                    "250401A1F6",
                    ChargeCategory.DEDUCT,
                    50000L,
                    ChargeTransactionStatus.PENDING
                )
            );

            BbanglePageResponse<ChargeTransactionResponse> pageResponse =
                new BbanglePageResponse<>(transactions, 0, 20, 1, 2L);
            ChargeBalanceResponse response = new ChargeBalanceResponse(balance, pageResponse);

            given(chargeService.getChargeBalance(any(), any(), any(), any(Pageable.class)))
                .willReturn(response);

            // When & Then
            mockMvc.perform(get(BASE_URL)
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.pageResponse.content[0].category").value("ACCUMULATE"))
                .andExpect(jsonPath("$.result.pageResponse.content[1].category").value("DEDUCT"))
                .andExpect(jsonPath("$.result.pageResponse.content[1].status").value("PENDING"));
        }
    }

    @Nested
    @DisplayName("충전금 출금 신청")
    class RequestWithdrawal {

        @Test
        @DisplayName("출금 신청 성공")
        @WithMockUser(roles = "SELLER")
        void requestWithdrawal_success() throws Exception {
            // Given
            WithdrawalRequest request = new WithdrawalRequest(
                new BigDecimal("50000"), "국민은행", "홍길동", "123456789012");

            willDoNothing().given(chargeService).requestWithdrawal(any());

            // When & Then
            mockMvc.perform(post(WITHDRAWAL_URL)
                    .header("X-Transaction-Id", TEST_TRANSACTION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonDataEncoder.encode(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("SUCCESS"));
        }

        @Test
        @DisplayName("실패 - X-Transaction-Id 헤더 누락")
        @WithMockUser(roles = "SELLER")
        void requestWithdrawal_missingTransactionIdHeader_badRequest() throws Exception {
            // Given
            WithdrawalRequest request = new WithdrawalRequest(
                new BigDecimal("50000"), "국민은행", "홍길동", "123456789012");

            // When & Then
            mockMvc.perform(post(WITHDRAWAL_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonDataEncoder.encode(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 출금 금액 누락")
        @WithMockUser(roles = "SELLER")
        void requestWithdrawal_missingWithdrawalAmount_badRequest() throws Exception {
            // Given
            WithdrawalRequest request = new WithdrawalRequest(
                null, "국민은행", "홍길동", "123456789012");

            // When & Then
            mockMvc.perform(post(WITHDRAWAL_URL)
                    .header("X-Transaction-Id", TEST_TRANSACTION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonDataEncoder.encode(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 출금 금액 0 이하")
        @WithMockUser(roles = "SELLER")
        void requestWithdrawal_zeroWithdrawalAmount_badRequest() throws Exception {
            // Given
            WithdrawalRequest request = new WithdrawalRequest(
                BigDecimal.ZERO, "국민은행", "홍길동", "123456789012");

            // When & Then
            mockMvc.perform(post(WITHDRAWAL_URL)
                    .header("X-Transaction-Id", TEST_TRANSACTION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonDataEncoder.encode(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 계좌번호에 문자 포함")
        @WithMockUser(roles = "SELLER")
        void requestWithdrawal_invalidAccountNumber_badRequest() throws Exception {
            // Given
            WithdrawalRequest request = new WithdrawalRequest(
                new BigDecimal("50000"), "국민은행", "홍길동", "12345-6789");

            // When & Then
            mockMvc.perform(post(WITHDRAWAL_URL)
                    .header("X-Transaction-Id", TEST_TRANSACTION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonDataEncoder.encode(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 중복 요청 (동일 transactionId)")
        @WithMockUser(roles = "SELLER")
        void requestWithdrawal_duplicateTransactionId_conflict() throws Exception {
            // Given
            WithdrawalRequest request = new WithdrawalRequest(
                new BigDecimal("50000"), "국민은행", "홍길동", "123456789012");

            willThrow(new BbangleException(BbangleErrorCode.CHARGE_WITHDRAWAL_DUPLICATED))
                .given(chargeService).requestWithdrawal(any());

            // When & Then
            mockMvc.perform(post(WITHDRAWAL_URL)
                    .header("X-Transaction-Id", TEST_TRANSACTION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonDataEncoder.encode(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-822))
                .andExpect(jsonPath("$.message").value("동일한 출금 요청이 처리 중입니다. 잠시 후 다시 시도해주세요."));
        }

        @Test
        @DisplayName("실패 - 잔액 부족")
        @WithMockUser(roles = "SELLER")
        void requestWithdrawal_insufficientBalance_badRequest() throws Exception {
            // Given
            WithdrawalRequest request = new WithdrawalRequest(
                new BigDecimal("999999999"), "국민은행", "홍길동", "123456789012");

            willThrow(new BbangleException(BbangleErrorCode.CHARGE_WITHDRAWAL_INSUFFICIENT_BALANCE))
                .given(chargeService).requestWithdrawal(any());

            // When & Then
            mockMvc.perform(post(WITHDRAWAL_URL)
                    .header("X-Transaction-Id", TEST_TRANSACTION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonDataEncoder.encode(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-823))
                .andExpect(jsonPath("$.message").value("충전금 잔액이 부족합니다."));
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void requestWithdrawal_unauthorized() throws Exception {
            // Given
            WithdrawalRequest request = new WithdrawalRequest(
                new BigDecimal("50000"), "국민은행", "홍길동", "123456789012");

            // When & Then
            mockMvc.perform(post(WITHDRAWAL_URL)
                    .header("X-Transaction-Id", TEST_TRANSACTION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonDataEncoder.encode(request)))
                .andExpect(status().isUnauthorized());
        }
    }
}
