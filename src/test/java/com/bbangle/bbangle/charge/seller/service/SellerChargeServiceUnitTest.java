package com.bbangle.bbangle.charge.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.bbangle.bbangle.charge.domain.ChargeBalance;
import com.bbangle.bbangle.charge.domain.ChargeHistory;
import com.bbangle.bbangle.charge.domain.enums.ChargeCategory;
import com.bbangle.bbangle.charge.domain.enums.ChargeTransactionStatus;
import com.bbangle.bbangle.charge.repository.ChargeBalanceRepository;
import com.bbangle.bbangle.charge.repository.ChargeHistoryRepository;
import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse;
import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse.ChargeTransactionResponse;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.charge.domain.ChargeBalanceFixture;
import com.bbangle.bbangle.fixture.charge.domain.ChargeHistoryFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DisplayName("[단위 테스트] SellerChargeService")
@ExtendWith(MockitoExtension.class)
class SellerChargeServiceUnitTest {

    @Mock
    private ChargeBalanceRepository chargeBalanceRepository;

    @Mock
    private ChargeHistoryRepository chargeHistoryRepository;

    @InjectMocks
    private SellerChargeService chargeService;

    private static final Long TEST_SELLER_ID = 1L;

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
}
