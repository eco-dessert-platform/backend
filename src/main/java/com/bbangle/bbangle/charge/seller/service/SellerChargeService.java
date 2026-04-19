package com.bbangle.bbangle.charge.seller.service;

import com.bbangle.bbangle.charge.domain.ChargeBalance;
import com.bbangle.bbangle.charge.domain.ChargeHistory;
import com.bbangle.bbangle.charge.domain.ChargeWithdrawalRequest;
import com.bbangle.bbangle.charge.domain.enums.ChargeTransactionStatus;
import com.bbangle.bbangle.charge.repository.ChargeBalanceRepository;
import com.bbangle.bbangle.charge.repository.ChargeHistoryRepository;
import com.bbangle.bbangle.charge.repository.ChargeWithdrawalRequestRepository;
import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse;
import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse.ChargeTransactionResponse;
import com.bbangle.bbangle.charge.seller.service.model.SellerChargeCommand.WithdrawalCommand;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import java.time.Duration;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerChargeService {

    private static final String WITHDRAWAL_REDIS_KEY_PREFIX = "seller:charge:withdrawal:";
    private static final Duration WITHDRAWAL_REDIS_TTL = Duration.ofMinutes(5);

    private final ChargeBalanceRepository chargeBalanceRepository;
    private final ChargeHistoryRepository chargeHistoryRepository;
    private final ChargeWithdrawalRequestRepository chargeWithdrawalRequestRepository;
    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional(readOnly = true)
    public ChargeBalanceResponse getChargeBalance(
        Long sellerId,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    ) {
        LocalDate queryEndDate = endDate != null ? endDate : LocalDate.now();
        LocalDate queryStartDate = startDate != null ? startDate : queryEndDate.minusDays(7);

        ChargeBalance chargeBalance = chargeBalanceRepository.findBySellerId(sellerId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.CHARGE_BALANCE_NOT_FOUND));

        Page<ChargeHistory> historyPage = chargeHistoryRepository.findBySellerIdAndBaseDateBetween(
            sellerId,
            queryStartDate,
            queryEndDate,
            pageable
        );

        Page<ChargeTransactionResponse> content = historyPage
            .map(history -> new ChargeTransactionResponse(
                history.getBaseDate(),
                String.valueOf(history.getReferenceId()),
                history.getCategory(),
                history.getAmount().longValue(),
                history.getStatus()
            ));

        BbanglePageResponse<ChargeTransactionResponse> pageResponse = BbanglePageResponse.of(content);
        return new ChargeBalanceResponse(chargeBalance.getBalance(), pageResponse);
    }

    // TODO: 계좌이체 외부 API 연동 필요.
    @Transactional
    public void requestWithdrawal(WithdrawalCommand command) {
        preventDuplicateRequest(command.transactionId(), command.sellerId());

        ChargeBalance chargeBalance = chargeBalanceRepository.findBySellerIdWithLock(command.sellerId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.CHARGE_BALANCE_NOT_FOUND));

        Seller seller = chargeBalance.getSeller();

        if (!chargeBalance.isSufficientBalance(command.withdrawalAmount())) {
            chargeWithdrawalRequestRepository.save(
                ChargeWithdrawalRequest.createFailure(
                    seller,
                    command.withdrawalAmount(),
                    command.bankName(),
                    command.accountHolder(),
                    command.accountNumber(),
                    "잔액부족"
                )
            );
            throw new BbangleException(BbangleErrorCode.CHARGE_WITHDRAWAL_INSUFFICIENT_BALANCE);
        }

        chargeBalance.withdraw(command.withdrawalAmount());

        ChargeWithdrawalRequest withdrawalRequest = chargeWithdrawalRequestRepository.save(
            ChargeWithdrawalRequest.createSuccess(
                seller,
                command.withdrawalAmount(),
                command.bankName(),
                command.accountHolder(),
                command.accountNumber()
            )
        );

        chargeHistoryRepository.save(
            ChargeHistory.fromWithdrawal(
                seller,
                command.withdrawalAmount(),
                chargeBalance.getBalance(),
                ChargeTransactionStatus.COMPLETED,
                LocalDate.now(),
                withdrawalRequest.getId()
            )
        );
    }

    private void preventDuplicateRequest(String transactionId, Long sellerId) {
        String redisKey = WITHDRAWAL_REDIS_KEY_PREFIX + transactionId;
        boolean isNew = Boolean.TRUE.equals(
            redisTemplate.opsForValue().setIfAbsent(redisKey, String.valueOf(sellerId), WITHDRAWAL_REDIS_TTL)
        );
        if (!isNew) {
            throw new BbangleException(BbangleErrorCode.CHARGE_WITHDRAWAL_DUPLICATED);
        }
    }

}
