package com.bbangle.bbangle.vat.seller.excel.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.vat.seller.controller.dto.request.SellerVatExcelType;
import com.bbangle.bbangle.vat.seller.excel.writer.SellerVatExcelWriter;
import com.bbangle.bbangle.vat.seller.provider.SellerVatSettlementProvider;
import com.bbangle.bbangle.vat.seller.provider.dto.SellerVatSettlementRow;
import com.bbangle.bbangle.vat.seller.service.model.SellerVatCommand.SellerVatSearchCommand;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("[Service] SellerVatExcelService")
class SellerVatExcelServiceTest {

    @Mock
    private SellerVatSettlementProvider settlementProvider;

    @Mock
    private SellerVatExcelWriter excelWriter;

    @Captor
    private ArgumentCaptor<List<SellerVatSettlementRow>> rowsCaptor;

    @InjectMocks
    private SellerVatExcelService sellerVatExcelService;

    @Test
    @DisplayName("provider에서 조회한 정산 row를 엑셀 writer로 전달한다")
    void writeExcel_delegatesFetchedRowsToWriter() throws Exception {
        // given
        Long sellerId = 1L;
        YearMonth startMonth = YearMonth.of(2025, 3);
        YearMonth endMonth = YearMonth.of(2025, 4);
        SellerVatSearchCommand command = SellerVatSearchCommand.builder()
            .sellerId(sellerId)
            .startMonth(startMonth)
            .endMonth(endMonth)
            .build();

        List<SellerVatSettlementRow> rows = List.of(new SellerVatSettlementRow(
            sellerId,
            LocalDate.of(2025, 4, 5),
            "SETTLE-202504-0001",
            800000L,
            100000L,
            500000L,
            180000L,
            90000L,
            130000L
        ));

        given(settlementProvider.findSettlements(sellerId, startMonth, endMonth))
            .willReturn(rows);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // when
        sellerVatExcelService.writeExcel(command, SellerVatExcelType.MONTHLY, outputStream);

        // then
        verify(settlementProvider).findSettlements(sellerId, startMonth, endMonth);
        verify(excelWriter).write(
            org.mockito.ArgumentMatchers.eq(SellerVatExcelType.MONTHLY),
            rowsCaptor.capture(),
            org.mockito.ArgumentMatchers.eq(outputStream)
        );
        org.assertj.core.api.Assertions.assertThat(rowsCaptor.getValue()).containsExactlyElementsOf(rows);
    }
}
