package com.bbangle.bbangle.paymenthold.seller.excel.service;

import com.bbangle.bbangle.paymenthold.repository.PaymentHoldRepository;
import com.bbangle.bbangle.paymenthold.seller.controller.dto.response.PaymentHoldResponse.PaymentHoldSummary;
import com.bbangle.bbangle.paymenthold.seller.excel.service.model.PaymentHoldExcelSearchCommand;
import com.bbangle.bbangle.paymenthold.seller.excel.writer.PaymentHoldExcelWriter;
import com.bbangle.bbangle.paymenthold.seller.service.PaymentHoldSearchValidator;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerPaymentHoldExcelService {

    private final PaymentHoldRepository paymentHoldRepository;
    private final PaymentHoldExcelWriter paymentHoldExcelWriter;
    private final PaymentHoldSearchValidator paymentHoldSearchValidator;

    /**
     * 엑셀 다운로드 요청의 조회 조건을 검증한다.
     * ID 검색이 아니면 시작일/종료일 필수 여부와 최대 1개월 조건을 함께 확인한다.
     */
    public void validateExcelDownload(PaymentHoldExcelSearchCommand command) {
        paymentHoldSearchValidator.validateExcel(command);
    }

    /**
     * 조건에 맞는 지급보류 내역을 전건 조회해 엑셀 워크북으로 출력한다.
     * 목록 API와 동일한 응답 컬럼을 재사용해 조회 화면과 엑셀 컬럼을 맞춘다.
     */
    public void writePaymentHoldExcel(
        PaymentHoldExcelSearchCommand command,
        OutputStream outputStream
    ) throws IOException {
        List<PaymentHoldSummary> rows = paymentHoldRepository.findAllForExcel(command)
            .stream()
            .map(PaymentHoldSummary::from)
            .toList();

        paymentHoldExcelWriter.write(rows, outputStream);
    }
}
