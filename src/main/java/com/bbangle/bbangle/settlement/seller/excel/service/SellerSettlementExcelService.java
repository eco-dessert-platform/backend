package com.bbangle.bbangle.settlement.seller.excel.service;

import com.bbangle.bbangle.settlement.domain.DailySettlement;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.DailySettlementResponse.DailySettlementSummary;
import com.bbangle.bbangle.settlement.seller.excel.repository.DailySettlementExcelDSLRepository;
import com.bbangle.bbangle.settlement.seller.excel.service.model.DailySettlementExcelSearchCommand;
import com.bbangle.bbangle.settlement.seller.excel.writer.DailySettlementExcelWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 일별 정산 내역 엑셀 다운로드 서비스.
 * 조건에 맞는 전체 정산 내역을 조회한 후 엑셀로 변환해 outputStream에 출력한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerSettlementExcelService {

    private final DailySettlementExcelDSLRepository excelRepository;
    private final DailySettlementExcelWriter excelWriter;

    /**
     * 정산 내역을 조회하고 엑셀 파일을 outputStream에 직접 출력한다.
     * DailySettlementSummary.from()을 재사용해 화면 조회 API와 동일한 컬럼을 보장한다.
     *
     * @param command      셀러ID + 날짜 범위 조회 조건
     * @param outputStream HTTP 응답 출력 스트림
     */
    public void writeDailySettlementExcel(
        DailySettlementExcelSearchCommand command,
        OutputStream outputStream
    ) throws IOException {
        // 전체 데이터를 먼저 조회한 뒤 write를 시작 — write 도중 예외가 발생하면
        // 이미 응답이 커밋된 상태가 되므로, 사전에 데이터를 확보해 그 위험을 최소화
        List<DailySettlementSummary> rows = excelRepository.findAllForExcel(command)
            .stream()
            .map(DailySettlementSummary::from)
            .toList();

        excelWriter.write(rows, outputStream);
    }

}
