package com.bbangle.bbangle.charge.seller.service;

import com.bbangle.bbangle.charge.domain.ChargeBalance;
import com.bbangle.bbangle.charge.domain.ChargeHistory;
import com.bbangle.bbangle.charge.repository.ChargeBalanceRepository;
import com.bbangle.bbangle.charge.repository.ChargeHistoryRepository;
import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse;
import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse.ChargeTransactionResponse;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerChargeService {

    private final ChargeBalanceRepository chargeBalanceRepository;
    private final ChargeHistoryRepository chargeHistoryRepository;

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

}
