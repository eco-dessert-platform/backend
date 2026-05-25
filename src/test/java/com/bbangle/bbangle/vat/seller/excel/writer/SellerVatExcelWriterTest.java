package com.bbangle.bbangle.vat.seller.excel.writer;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.vat.seller.controller.dto.request.SellerVatExcelType;
import com.bbangle.bbangle.vat.seller.provider.dto.SellerVatSettlementRow;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[Writer] SellerVatExcelWriter")
class SellerVatExcelWriterTest {

    private final SellerVatExcelWriter writer = new SellerVatExcelWriter();

    @Test
    @DisplayName("MONTHLY 엑셀은 월별 합산 row를 최신 월부터 작성한다")
    void writeMonthlyExcel_aggregatesRowsByMonth() throws Exception {
        // when
        Sheet sheet = writeAndGetSheet(SellerVatExcelType.MONTHLY, sampleRows());

        // then
        assertThat(sheet.getSheetName()).isEqualTo("MONTHLY");
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("일자");
        assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("과세 매출금액");
        assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("2025-04");
        assertThat(sheet.getRow(1).getCell(1).getNumericCellValue()).isEqualTo(1875000D);
        assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("2025-03");
    }

    @Test
    @DisplayName("ORDER 엑셀은 정산일과 정산번호를 포함한 건별 row를 작성한다")
    void writeOrderExcel_writesSettlementRows() throws Exception {
        // when
        Sheet sheet = writeAndGetSheet(SellerVatExcelType.ORDER, sampleRows());

        // then
        assertThat(sheet.getSheetName()).isEqualTo("ORDER");
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("정산일");
        assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("정산번호");
        assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("2025-04-18");
        assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("SETTLE-202504-0002");
    }

    @Test
    @DisplayName("데이터가 없으면 헤더만 있는 엑셀을 생성한다")
    void writeExcel_writesOnlyHeaderWhenRowsEmpty() throws Exception {
        // when
        Sheet sheet = writeAndGetSheet(SellerVatExcelType.DAILY, List.of());

        // then
        assertThat(sheet.getLastRowNum()).isZero();
        assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("일자");
    }

    private Sheet writeAndGetSheet(
        SellerVatExcelType type,
        List<SellerVatSettlementRow> rows
    ) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writer.write(type, rows, outputStream);

        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()));
        return workbook.getSheetAt(0);
    }

    private List<SellerVatSettlementRow> sampleRows() {
        return List.of(
            new SellerVatSettlementRow(
                1L,
                LocalDate.of(2025, 4, 5),
                "SETTLE-202504-0001",
                800000L,
                100000L,
                500000L,
                180000L,
                90000L,
                130000L
            ),
            new SellerVatSettlementRow(
                1L,
                LocalDate.of(2025, 4, 18),
                "SETTLE-202504-0002",
                1075000L,
                220000L,
                750000L,
                250000L,
                170000L,
                125000L
            ),
            new SellerVatSettlementRow(
                1L,
                LocalDate.of(2025, 3, 9),
                "SETTLE-202503-0001",
                1640000L,
                210000L,
                1120000L,
                350000L,
                180000L,
                200000L
            )
        );
    }
}
