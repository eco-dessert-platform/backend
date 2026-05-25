package com.bbangle.bbangle.vat.seller.excel.writer;

import com.bbangle.bbangle.vat.seller.controller.dto.request.SellerVatExcelType;
import com.bbangle.bbangle.vat.seller.controller.dto.response.SellerVatMonthlyItem;
import com.bbangle.bbangle.vat.seller.provider.dto.SellerVatSettlementRow;
import com.bbangle.bbangle.vat.seller.service.model.SellerVatAmountSum;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * 판매자 부가세 신고 내역을 엑셀 파일로 작성한다.
 * 데이터가 없는 경우에도 헤더만 포함한 정상 xlsx 파일을 생성한다.
 */
@Component
public class SellerVatExcelWriter {

    private static final int COLUMN_WIDTH = 256 * 22;
    private static final String MONEY_FORMAT = "#,##0";

    public void write(
        SellerVatExcelType type,
        List<SellerVatSettlementRow> rows,
        OutputStream outputStream
    ) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet(type.name());
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            switch (type) {
                case MONTHLY -> writeMonthlySheet(sheet, rows, headerStyle, moneyStyle);
                case DAILY -> writeDailySheet(sheet, rows, headerStyle, moneyStyle);
                case ORDER -> writeOrderSheet(sheet, rows, headerStyle, moneyStyle);
            }

            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    private void writeMonthlySheet(
        Sheet sheet,
        List<SellerVatSettlementRow> rows,
        CellStyle headerStyle,
        CellStyle moneyStyle
    ) {
        String[] headers = commonHeaders("일자");
        writeHeader(sheet, headers, headerStyle);

        Map<YearMonth, SellerVatAmountSum> monthlySums = new TreeMap<>(Comparator.reverseOrder());
        for (SellerVatSettlementRow row : rows) {
            monthlySums.computeIfAbsent(YearMonth.from(row.settlementDate()), ignored -> new SellerVatAmountSum())
                .add(row);
        }

        int rowIndex = 1;
        for (Map.Entry<YearMonth, SellerVatAmountSum> entry : monthlySums.entrySet()) {
            SellerVatMonthlyItem item = entry.getValue().toMonthlyItem(entry.getKey());
            Row row = sheet.createRow(rowIndex++);
            writeCommonCells(row, item.month(), item, moneyStyle);
        }

        setColumnWidths(sheet, headers.length);
    }

    private void writeDailySheet(
        Sheet sheet,
        List<SellerVatSettlementRow> rows,
        CellStyle headerStyle,
        CellStyle moneyStyle
    ) {
        String[] headers = commonHeaders("일자");
        writeHeader(sheet, headers, headerStyle);

        Map<LocalDate, SellerVatAmountSum> dailySums = new TreeMap<>(Comparator.reverseOrder());
        for (SellerVatSettlementRow row : rows) {
            dailySums.computeIfAbsent(row.settlementDate(), ignored -> new SellerVatAmountSum())
                .add(row);
        }

        int rowIndex = 1;
        for (Map.Entry<LocalDate, SellerVatAmountSum> entry : dailySums.entrySet()) {
            SellerVatMonthlyItem item = entry.getValue().toMonthlyItem(YearMonth.from(entry.getKey()));
            Row row = sheet.createRow(rowIndex++);
            writeCommonCells(row, entry.getKey().toString(), item, moneyStyle);
        }

        setColumnWidths(sheet, headers.length);
    }

    private void writeOrderSheet(
        Sheet sheet,
        List<SellerVatSettlementRow> rows,
        CellStyle headerStyle,
        CellStyle moneyStyle
    ) {
        String[] headers = {
            "정산일",
            "정산번호",
            "과세 매출금액",
            "면세 매출금액",
            "신용카드",
            "현금영수증 소득공제",
            "현금영수증 지출증빙",
            "기타"
        };
        writeHeader(sheet, headers, headerStyle);

        List<SellerVatSettlementRow> sortedRows = rows.stream()
            .sorted(Comparator.comparing(SellerVatSettlementRow::settlementDate)
                .reversed()
                .thenComparing(SellerVatSettlementRow::settlementNo, Comparator.reverseOrder()))
            .toList();

        for (int index = 0; index < sortedRows.size(); index++) {
            SellerVatSettlementRow settlement = sortedRows.get(index);
            Row row = sheet.createRow(index + 1);
            row.createCell(0).setCellValue(settlement.settlementDate().toString());
            row.createCell(1).setCellValue(settlement.settlementNo());
            writeMoneyCell(row, 2, settlement.taxableSalesAmount(), moneyStyle);
            writeMoneyCell(row, 3, settlement.taxFreeSalesAmount(), moneyStyle);
            writeMoneyCell(row, 4, settlement.creditCardAmount(), moneyStyle);
            writeMoneyCell(row, 5, settlement.cashReceiptIncomeDeductionAmount(), moneyStyle);
            writeMoneyCell(row, 6, settlement.cashReceiptExpenseProofAmount(), moneyStyle);
            writeMoneyCell(row, 7, settlement.etcAmount(), moneyStyle);
        }

        setColumnWidths(sheet, headers.length);
    }

    private void writeCommonCells(
        Row row,
        String dateValue,
        SellerVatMonthlyItem item,
        CellStyle moneyStyle
    ) {
        row.createCell(0).setCellValue(dateValue);
        writeMoneyCell(row, 1, item.taxableSalesAmount(), moneyStyle);
        writeMoneyCell(row, 2, item.taxFreeSalesAmount(), moneyStyle);
        writeMoneyCell(row, 3, item.creditCardAmount(), moneyStyle);
        writeMoneyCell(row, 4, item.cashReceiptIncomeDeductionAmount(), moneyStyle);
        writeMoneyCell(row, 5, item.cashReceiptExpenseProofAmount(), moneyStyle);
        writeMoneyCell(row, 6, item.etcAmount(), moneyStyle);
    }

    private String[] commonHeaders(String dateHeader) {
        return new String[] {
            dateHeader,
            "과세 매출금액",
            "면세 매출금액",
            "신용카드",
            "현금영수증 소득공제",
            "현금영수증 지출증빙",
            "기타"
        };
    }

    private void writeHeader(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int index = 0; index < headers.length; index++) {
            Cell cell = headerRow.createCell(index);
            cell.setCellValue(headers[index]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeMoneyCell(Row row, int columnIndex, Long amount, CellStyle moneyStyle) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(amount == null ? 0D : amount.doubleValue());
        cell.setCellStyle(moneyStyle);
    }

    private void setColumnWidths(Sheet sheet, int columnCount) {
        for (int index = 0; index < columnCount; index++) {
            sheet.setColumnWidth(index, COLUMN_WIDTH);
        }
    }

    private CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        style.setFont(boldFont);

        return style;
    }

    private CellStyle createMoneyStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat(MONEY_FORMAT));
        return style;
    }
}
