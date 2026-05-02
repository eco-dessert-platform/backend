package com.bbangle.bbangle.charge.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.bbangle.bbangle.charge.domain.ChargeBalance;
import com.bbangle.bbangle.charge.domain.ChargeHistory;
import com.bbangle.bbangle.charge.domain.ChargeWithdrawalRequest;
import com.bbangle.bbangle.charge.domain.enums.ChargeCategory;
import com.bbangle.bbangle.charge.domain.enums.ChargeTransactionStatus;
import com.bbangle.bbangle.charge.repository.ChargeBalanceRepository;
import com.bbangle.bbangle.charge.repository.ChargeHistoryRepository;
import com.bbangle.bbangle.charge.repository.ChargeWithdrawalRequestRepository;
import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse;
import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse.ChargeTransactionResponse;
import com.bbangle.bbangle.charge.seller.service.model.SellerChargeCommand.WithdrawalCommand;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.charge.domain.ChargeBalanceFixture;
import com.bbangle.bbangle.fixture.charge.domain.ChargeHistoryFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[단위 테스트] SellerChargeService")
@ExtendWith(MockitoExtension.class)
class SellerChargeServiceUnitTest {

    @Mock
    private ChargeBalanceRepository chargeBalanceRepository;

    @Mock
    private ChargeHistoryRepository chargeHistoryRepository;

    @Mock
    private ChargeWithdrawalRequestRepository chargeWithdrawalRequestRepository;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private SellerChargeService chargeService;

    private static final Long TEST_SELLER_ID = 1L;
    private static final String TEST_TRANSACTION_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Nested
    @DisplayName("충전금 현황 조회")
    class GetChargeBalance {

        @Test
        @DisplayName("정상 조회 - 기본 날짜로 조회")
        void testGetChargeBalanceSuccess() {
            // Given
            Seller seller = SellerFixture.withId(SellerFixture.defaultSeller(), TEST_SELLER_ID);
            ChargeBalance chargeBalance = ChargeBalanceFixture.withId(
                ChargeBalanceFixture.createDefault(seller), 1L);

            LocalDate today = LocalDate.now();
            ChargeHistory history1 = ChargeHistoryFixture.withId(
                ChargeHistoryFixture.createAccumulate(seller, today), 1L);
            ChargeHistory history2 = ChargeHistoryFixture.withId(
                ChargeHistoryFixture.createDeduct(seller, today.minusDays(1)), 2L);

            Page<ChargeHistory> historyPage = new PageImpl<>(
                List.of(history1, history2),
                PageRequest.of(0, 20),
                2
            );

            given(chargeBalanceRepository.findBySellerId(TEST_SELLER_ID))
                .willReturn(Optional.of(chargeBalance));
            given(chargeHistoryRepository.findBySellerIdAndBaseDateBetween(
                anyLong(), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .willReturn(historyPage);

            // When
            ChargeBalanceResponse response = chargeService.getChargeBalance(
                TEST_SELLER_ID,
                null,
                null,
                PageRequest.of(0, 20)
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.chargeBalance()).isEqualTo(new BigDecimal("100000"));
            assertThat(response.pageResponse().content()).hasSize(2);
            assertThat(response.pageResponse().totalElements()).isEqualTo(2);
            assertThat(response.pageResponse().page()).isEqualTo(0);
        }

        @Test
        @DisplayName("날짜 범위 지정으로 조회")
        void testGetChargeBalanceWithDateRange() {
            // Given
            Seller seller = SellerFixture.withId(SellerFixture.defaultSeller(), TEST_SELLER_ID);
            ChargeBalance chargeBalance = ChargeBalanceFixture.createDefault(seller);

            LocalDate startDate = LocalDate.of(2025, 3, 1);
            LocalDate endDate = LocalDate.of(2025, 3, 7);

            ChargeHistory history = ChargeHistoryFixture.withId(
                ChargeHistoryFixture.createAccumulate(seller, endDate), 1L);

            Page<ChargeHistory> historyPage = new PageImpl<>(
                List.of(history),
                PageRequest.of(0, 20),
                1
            );

            given(chargeBalanceRepository.findBySellerId(TEST_SELLER_ID))
                .willReturn(Optional.of(chargeBalance));
            given(chargeHistoryRepository.findBySellerIdAndBaseDateBetween(
                anyLong(), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .willReturn(historyPage);

            // When
            ChargeBalanceResponse response = chargeService.getChargeBalance(
                TEST_SELLER_ID,
                startDate,
                endDate,
                PageRequest.of(0, 20)
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.pageResponse().content()).hasSize(1);
            assertThat(response.pageResponse().totalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("거래내역이 없는 경우")
        void testGetChargeBalanceEmptyHistory() {
            // Given
            Seller seller = SellerFixture.withId(SellerFixture.defaultSeller(), TEST_SELLER_ID);
            ChargeBalance chargeBalance = ChargeBalanceFixture.createDefault(seller);

            Page<ChargeHistory> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0
            );

            given(chargeBalanceRepository.findBySellerId(TEST_SELLER_ID))
                .willReturn(Optional.of(chargeBalance));
            given(chargeHistoryRepository.findBySellerIdAndBaseDateBetween(
                anyLong(), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .willReturn(emptyPage);

            // When
            ChargeBalanceResponse response = chargeService.getChargeBalance(
                TEST_SELLER_ID,
                null,
                null,
                PageRequest.of(0, 20)
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.chargeBalance()).isEqualTo(new BigDecimal("100000"));
            assertThat(response.pageResponse().content()).isEmpty();
            assertThat(response.pageResponse().totalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("충전금 잔액 없음 - 예외 발생")
        void testGetChargeBalanceNotFound() {
            // Given
            given(chargeBalanceRepository.findBySellerId(TEST_SELLER_ID))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> chargeService.getChargeBalance(
                TEST_SELLER_ID,
                null,
                null,
                PageRequest.of(0, 20)
            ))
                .isInstanceOf(BbangleException.class);
        }

        @Test
        @DisplayName("다양한 거래 상태 조회 (COMPLETED, PENDING)")
        void testGetChargeBalanceWithDifferentStatus() {
            // Given
            Seller seller = SellerFixture.withId(SellerFixture.defaultSeller(), TEST_SELLER_ID);
            ChargeBalance chargeBalance = ChargeBalanceFixture.createDefault(seller);

            LocalDate today = LocalDate.now();
            ChargeHistory completedHistory = ChargeHistoryFixture.withId(
                ChargeHistoryFixture.createAccumulate(seller, today), 1L);
            ChargeHistory pendingHistory = ChargeHistoryFixture.withId(
                ChargeHistoryFixture.createPending(seller, today.minusDays(1)), 2L);

            Page<ChargeHistory> historyPage = new PageImpl<>(
                List.of(completedHistory, pendingHistory),
                PageRequest.of(0, 20),
                2
            );

            given(chargeBalanceRepository.findBySellerId(TEST_SELLER_ID))
                .willReturn(Optional.of(chargeBalance));
            given(chargeHistoryRepository.findBySellerIdAndBaseDateBetween(
                anyLong(), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .willReturn(historyPage);

            // When
            ChargeBalanceResponse response = chargeService.getChargeBalance(
                TEST_SELLER_ID,
                null,
                null,
                PageRequest.of(0, 20)
            );

            // Then
            assertThat(response.pageResponse().content()).hasSize(2);

            ChargeTransactionResponse first = response.pageResponse().content().get(0);
            assertThat(first.status()).isEqualTo(ChargeTransactionStatus.COMPLETED);

            ChargeTransactionResponse second = response.pageResponse().content().get(1);
            assertThat(second.status()).isEqualTo(ChargeTransactionStatus.PENDING);
        }

        @Test
        @DisplayName("거래 구분 검증 (ACCUMULATE, DEDUCT)")
        void testGetChargeBalanceWithDifferentCategories() {
            // Given
            Seller seller = SellerFixture.withId(SellerFixture.defaultSeller(), TEST_SELLER_ID);
            ChargeBalance chargeBalance = ChargeBalanceFixture.createDefault(seller);

            LocalDate today = LocalDate.now();
            ChargeHistory accumulateHistory = ChargeHistoryFixture.withId(
                ChargeHistoryFixture.createAccumulate(seller, today), 1L);
            ChargeHistory deductHistory = ChargeHistoryFixture.withId(
                ChargeHistoryFixture.createDeduct(seller, today.minusDays(1)), 2L);

            Page<ChargeHistory> historyPage = new PageImpl<>(
                List.of(accumulateHistory, deductHistory),
                PageRequest.of(0, 20),
                2
            );

            given(chargeBalanceRepository.findBySellerId(TEST_SELLER_ID))
                .willReturn(Optional.of(chargeBalance));
            given(chargeHistoryRepository.findBySellerIdAndBaseDateBetween(
                anyLong(), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .willReturn(historyPage);

            // When
            ChargeBalanceResponse response = chargeService.getChargeBalance(
                TEST_SELLER_ID,
                null,
                null,
                PageRequest.of(0, 20)
            );

            // Then
            ChargeTransactionResponse first = response.pageResponse().content().get(0);
            assertThat(first.category()).isEqualTo(ChargeCategory.ACCUMULATE);

            ChargeTransactionResponse second = response.pageResponse().content().get(1);
            assertThat(second.category()).isEqualTo(ChargeCategory.DEDUCT);
        }

        @Test
        @DisplayName("페이지네이션 검증")
        void testGetChargeBalancePagination() {
            // Given
            Seller seller = SellerFixture.withId(SellerFixture.defaultSeller(), TEST_SELLER_ID);
            ChargeBalance chargeBalance = ChargeBalanceFixture.createDefault(seller);

            Page<ChargeHistory> historyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(2, 10),
                100
            );

            given(chargeBalanceRepository.findBySellerId(TEST_SELLER_ID))
                .willReturn(Optional.of(chargeBalance));
            given(chargeHistoryRepository.findBySellerIdAndBaseDateBetween(
                anyLong(), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .willReturn(historyPage);

            // When
            ChargeBalanceResponse response = chargeService.getChargeBalance(
                TEST_SELLER_ID,
                null,
                null,
                PageRequest.of(2, 10)
            );

            // Then
            assertThat(response.pageResponse().page()).isEqualTo(2);
            assertThat(response.pageResponse().size()).isEqualTo(10);
            assertThat(response.pageResponse().totalElements()).isEqualTo(100);
            assertThat(response.pageResponse().totalPages()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("충전금 출금 신청")
    class RequestWithdrawal {

        @Test
        @DisplayName("출금 성공 - 잔액 차감, 출금 이력 및 거래 내역 저장")
        void requestWithdrawal_success() {
            // Given
            Seller seller = SellerFixture.withId(SellerFixture.defaultSeller(), TEST_SELLER_ID);
            ChargeBalance chargeBalance = ChargeBalanceFixture.createWithBalance(seller, new BigDecimal("100000"));
            WithdrawalCommand command = createCommand(new BigDecimal("50000"));

            ChargeWithdrawalRequest savedRequest = ChargeWithdrawalRequest.createSuccess(
                seller, command.withdrawalAmount(), command.bankName(),
                command.accountHolder(), command.accountNumber());
            ReflectionTestUtils.setField(savedRequest, "id", 1L);

            given(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(
                true);
            given(chargeBalanceRepository.findBySellerIdWithLock(TEST_SELLER_ID))
                .willReturn(Optional.of(chargeBalance));
            given(chargeWithdrawalRequestRepository.save(any())).willReturn(savedRequest);

            // When
            chargeService.requestWithdrawal(command);

            // Then
            assertThat(chargeBalance.getBalance()).isEqualByComparingTo(new BigDecimal("50000"));

            ArgumentCaptor<ChargeWithdrawalRequest> withdrawalCaptor =
                ArgumentCaptor.forClass(ChargeWithdrawalRequest.class);
            then(chargeWithdrawalRequestRepository).should().save(withdrawalCaptor.capture());
            assertThat(withdrawalCaptor.getValue().getSuccess()).isTrue();
            assertThat(withdrawalCaptor.getValue().getFailureReason()).isNull();

            ArgumentCaptor<ChargeHistory> historyCaptor =
                ArgumentCaptor.forClass(ChargeHistory.class);
            then(chargeHistoryRepository).should().save(historyCaptor.capture());
            assertThat(historyCaptor.getValue().getCategory()).isEqualTo(ChargeCategory.DEDUCT);
            assertThat(historyCaptor.getValue().getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
            assertThat(historyCaptor.getValue().getBalanceAfter()).isEqualByComparingTo(new BigDecimal("50000"));
            assertThat(historyCaptor.getValue().getStatus()).isEqualTo(ChargeTransactionStatus.COMPLETED);
        }

        @Test
        @DisplayName("중복 요청 - 동일 transactionId 재요청 시 예외 발생")
        void requestWithdrawal_duplicateTransactionId_throwsDuplicated() {
            // Given
            WithdrawalCommand command = createCommand(new BigDecimal("50000"));

            given(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willReturn(false);

            // When & Then
            assertThatThrownBy(() -> chargeService.requestWithdrawal(command))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CHARGE_WITHDRAWAL_DUPLICATED);

            then(chargeBalanceRepository).should(never()).findBySellerIdWithLock(anyLong());
        }

        @Test
        @DisplayName("충전금 잔액 정보 없음 - 예외 발생")
        void requestWithdrawal_chargeBalanceNotFound_throwsException() {
            // Given
            WithdrawalCommand command = createCommand(new BigDecimal("50000"));

            given(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willReturn(true);
            given(chargeBalanceRepository.findBySellerIdWithLock(TEST_SELLER_ID))
                .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> chargeService.requestWithdrawal(command))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CHARGE_BALANCE_NOT_FOUND);

            then(chargeWithdrawalRequestRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("잔액 부족 - 실패 이력 저장 후 예외 발생")
        void requestWithdrawal_insufficientBalance_savesFailureAndThrowsException() {
            // Given
            Seller seller = SellerFixture.withId(SellerFixture.defaultSeller(), TEST_SELLER_ID);
            ChargeBalance chargeBalance = ChargeBalanceFixture.createWithBalance(seller, new BigDecimal("10000"));
            WithdrawalCommand command = createCommand(new BigDecimal("50000"));

            ChargeWithdrawalRequest failedRequest = ChargeWithdrawalRequest.createFailure(
                seller, command.withdrawalAmount(), command.bankName(),
                command.accountHolder(), command.accountNumber(), "잔액부족");
            ReflectionTestUtils.setField(failedRequest, "id", 1L);

            given(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willReturn(true);
            given(chargeBalanceRepository.findBySellerIdWithLock(TEST_SELLER_ID))
                .willReturn(Optional.of(chargeBalance));
            given(chargeWithdrawalRequestRepository.save(any())).willReturn(failedRequest);

            // When & Then
            assertThatThrownBy(() -> chargeService.requestWithdrawal(command))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CHARGE_WITHDRAWAL_INSUFFICIENT_BALANCE);

            ArgumentCaptor<ChargeWithdrawalRequest> captor =
                ArgumentCaptor.forClass(ChargeWithdrawalRequest.class);
            then(chargeWithdrawalRequestRepository).should().save(captor.capture());
            assertThat(captor.getValue().getSuccess()).isFalse();
            assertThat(captor.getValue().getFailureReason()).isEqualTo("잔액부족");

            then(chargeHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("잔액 부족 - 잔액은 차감되지 않음")
        void requestWithdrawal_insufficientBalance_balanceNotChanged() {
            // Given
            Seller seller = SellerFixture.withId(SellerFixture.defaultSeller(), TEST_SELLER_ID);
            BigDecimal originalBalance = new BigDecimal("10000");
            ChargeBalance chargeBalance = ChargeBalanceFixture.createWithBalance(seller, originalBalance);
            WithdrawalCommand command = createCommand(new BigDecimal("50000"));

            given(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willReturn(true);
            given(chargeBalanceRepository.findBySellerIdWithLock(TEST_SELLER_ID))
                .willReturn(Optional.of(chargeBalance));

            // When & Then
            assertThatThrownBy(() -> chargeService.requestWithdrawal(command))
                .isInstanceOf(BbangleException.class);

            assertThat(chargeBalance.getBalance()).isEqualByComparingTo(originalBalance);
        }

        @Test
        @DisplayName("Redis 키에 sellerId가 값으로 저장됨")
        void requestWithdrawal_redisKeyContainsSellerIdAsValue() {
            // Given
            Seller seller = SellerFixture.withId(SellerFixture.defaultSeller(), TEST_SELLER_ID);
            ChargeBalance chargeBalance = ChargeBalanceFixture.createWithBalance(seller, new BigDecimal("100000"));
            WithdrawalCommand command = createCommand(new BigDecimal("50000"));

            ChargeWithdrawalRequest savedRequest = ChargeWithdrawalRequest.createSuccess(
                seller, command.withdrawalAmount(), command.bankName(),
                command.accountHolder(), command.accountNumber());
            ReflectionTestUtils.setField(savedRequest, "id", 1L);

            given(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willReturn(true);
            given(chargeBalanceRepository.findBySellerIdWithLock(TEST_SELLER_ID))
                .willReturn(Optional.of(chargeBalance));
            given(chargeWithdrawalRequestRepository.save(any())).willReturn(savedRequest);

            // When
            chargeService.requestWithdrawal(command);

            // Then
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
            then(redisTemplate.opsForValue()).should()
                .setIfAbsent(keyCaptor.capture(), valueCaptor.capture(), any(Duration.class));

            assertThat(keyCaptor.getValue())
                .isEqualTo("seller:charge:withdrawal:" + TEST_TRANSACTION_ID);
            assertThat(valueCaptor.getValue())
                .isEqualTo(String.valueOf(TEST_SELLER_ID));
        }

        private WithdrawalCommand createCommand(BigDecimal withdrawalAmount) {
            return WithdrawalCommand.builder()
                .sellerId(TEST_SELLER_ID)
                .transactionId(TEST_TRANSACTION_ID)
                .withdrawalAmount(withdrawalAmount)
                .bankName("국민은행")
                .accountHolder("홍길동")
                .accountNumber("123456789012")
                .build();
        }
    }
    
}
