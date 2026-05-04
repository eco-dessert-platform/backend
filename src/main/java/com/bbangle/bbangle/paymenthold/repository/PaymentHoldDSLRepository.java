package com.bbangle.bbangle.paymenthold.repository;

import com.bbangle.bbangle.paymenthold.domain.PaymentHold;
import com.bbangle.bbangle.paymenthold.seller.excel.service.model.PaymentHoldExcelSearchCommand;
import com.bbangle.bbangle.paymenthold.seller.service.model.PaymentHoldSearchCommand;
import java.util.List;
import org.springframework.data.domain.Page;

public interface PaymentHoldDSLRepository {

    Page<PaymentHold> searchPaymentHolds(PaymentHoldSearchCommand command);

    List<PaymentHold> findAllForExcel(PaymentHoldExcelSearchCommand command);
}
