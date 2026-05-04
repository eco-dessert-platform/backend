package com.bbangle.bbangle.paymenthold.seller.excel.writer;

import com.bbangle.bbangle.paymenthold.seller.controller.dto.response.PaymentHoldResponse.PaymentHoldSummary;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
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

@Component
public class PaymentHoldExcelWriter {

    private static final String SHEET_NAME = "지급보류 내역";
    private static final int COLUMN_WIDTH = 256 * 20;
    private static final String MONEY_FORMAT = "#,##0";

    public void write(List<PaymentHoldSummary> rows, OutputStream outputStream) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            setColumnWidths(sheet);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            writeHeader(sheet, headerStyle);
            writeBody(sheet, rows, moneyStyle);

            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    private void writeHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        PaymentHoldExcelColumn[] columns = PaymentHoldExcelColumn.values();

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i].getHeaderName());
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeBody(Sheet sheet, List<PaymentHoldSummary> rows, CellStyle moneyStyle) {
        PaymentHoldExcelColumn[] columns = PaymentHoldExcelColumn.values();

        for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
            Row row = sheet.createRow(rowIdx + 1);
            PaymentHoldSummary summary = rows.get(rowIdx);

            for (int colIdx = 0; colIdx < columns.length; colIdx++) {
                Cell cell = row.createCell(colIdx);
                Object value = columns[colIdx].extractValue(summary);

                if (value instanceof BigDecimal moneyValue) {
                    cell.setCellValue(moneyValue.doubleValue());
                    cell.setCellStyle(moneyStyle);
                } else if (value != null) {
                    cell.setCellValue(value.toString());
                }
            }
        }
    }

    private void setColumnWidths(Sheet sheet) {
        for (int i = 0; i < PaymentHoldExcelColumn.values().length; i++) {
            sheet.setColumnWidth(i, COLUMN_WIDTH);
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
