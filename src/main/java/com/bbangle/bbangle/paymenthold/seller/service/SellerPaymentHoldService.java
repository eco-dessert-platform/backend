package com.bbangle.bbangle.paymenthold.seller.service;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.paymenthold.domain.PaymentHold;
import com.bbangle.bbangle.paymenthold.repository.PaymentHoldRepository;
import com.bbangle.bbangle.paymenthold.seller.controller.dto.response.PaymentHoldResponse.PaymentHoldPageResponse;
import com.bbangle.bbangle.paymenthold.seller.controller.dto.response.PaymentHoldResponse.PaymentHoldSummary;
import com.bbangle.bbangle.paymenthold.seller.service.model.PaymentHoldSearchCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerPaymentHoldService {

    private final PaymentHoldRepository paymentHoldRepository;
    private final PaymentHoldSearchValidator paymentHoldSearchValidator;

    /**
     * 판매자 지급보류 목록을 페이지 단위로 조회한다.
     * ID 검색이면 기간 제한을 건너뛰고, 일반 조회면 validator에서 기간 규칙을 먼저 검증한다.
     */
    public PaymentHoldPageResponse getPaymentHolds(PaymentHoldSearchCommand command) {
        paymentHoldSearchValidator.validateLookup(command);

        Page<PaymentHold> entityPage = paymentHoldRepository.searchPaymentHolds(command);

        List<PaymentHoldSummary> summaries = entityPage.getContent().stream()
            .map(PaymentHoldSummary::from)
            .toList();

        return new PaymentHoldPageResponse(
            new BbanglePageResponse<>(
                summaries,
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalPages(),
                entityPage.getTotalElements()
            )
        );
    }
}
