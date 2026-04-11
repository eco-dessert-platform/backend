package com.bbangle.bbangle.settlement.seller.excel.writer;

import com.bbangle.bbangle.settlement.seller.controller.dto.response.SettlementItemResponse.SettlementItemSummary;
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

/**
 * 건별 정산 내역을 엑셀(.xlsx)로 작성하는 컴포넌트.
 * SXSSFWorkbook을 사용해 스트리밍 방식으로 작성하므로 대용량 데이터에도 OOM이 발생하지 않는다.
 */
@Component
public class SettlementItemExcelWriter {

    /** 엑셀 시트 이름 */
    private static final String SHEET_NAME = "건별 정산 내역";

    /** 컬럼 고정 너비 (1/256 단위, 20자 기준) */
    private static final int COLUMN_WIDTH = 256 * 20;

    /** 금액 표시 포맷: #,##0 (소수점 없이 천 단위 구분) */
    private static final String MONEY_FORMAT = "#,##0";

    /**
     * 건별 정산 내역 목록을 엑셀로 작성해 outputStream에 직접 출력한다.
     * 데이터가 0건이면 헤더만 작성된 빈 엑셀을 반환한다.
     *
     * @param rows         엑셀에 작성할 건별 정산 내역 목록
     * @param outputStream 응답에 사용할 출력 스트림
     */
    public void write(List<SettlementItemSummary> rows, OutputStream outputStream) throws IOException {
        // 100행 단위 스트리밍 — 100행을 넘으면 오래된 행부터 임시 파일로 flush
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);
            setColumnWidths(sheet);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle moneyStyle = createMoneyStyle(workbook);

            writeHeader(sheet, headerStyle);
            writeBody(sheet, rows, moneyStyle);

            workbook.write(outputStream);
            // 스트리밍 중 생성된 임시 파일을 명시적으로 정리
            workbook.dispose();
        }
    }

    /**
     * 헤더 행을 작성한다. (굵은 글씨 + 회색 배경 + 얇은 테두리 + 중앙 정렬)
     */
    private void writeHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        SettlementItemExcelColumn[] columns = SettlementItemExcelColumn.values();

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i].getHeaderName());
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * 데이터 행들을 작성한다.
     * 금액(BigDecimal) 값은 숫자 셀로, 나머지는 문자열 셀로 저장한다.
     */
    private void writeBody(Sheet sheet, List<SettlementItemSummary> rows, CellStyle moneyStyle) {
        SettlementItemExcelColumn[] columns = SettlementItemExcelColumn.values();

        // 헤더 다음 행(1번 인덱스)부터 데이터 작성
        for (int rowIdx = 0; rowIdx < rows.size(); rowIdx++) {
            Row row = sheet.createRow(rowIdx + 1);
            SettlementItemSummary summary = rows.get(rowIdx);

            for (int colIdx = 0; colIdx < columns.length; colIdx++) {
                Cell cell = row.createCell(colIdx);
                Object value = columns[colIdx].extractValue(summary);

                if (value instanceof BigDecimal moneyValue) {
                    // 금액은 숫자 셀로 저장 — 엑셀에서 SUM/정렬 가능
                    cell.setCellValue(moneyValue.doubleValue());
                    cell.setCellStyle(moneyStyle);
                } else if (value instanceof Integer intValue) {
                    // 정수 값(수량 등)도 숫자 셀로 저장
                    cell.setCellValue(intValue.doubleValue());
                } else if (value != null) {
                    cell.setCellValue(value.toString());
                }
                // value가 null이면 빈 셀 유지
            }
        }
    }

    /** 모든 컬럼의 너비를 고정값으로 설정한다. */
    private void setColumnWidths(Sheet sheet) {
        for (int i = 0; i < SettlementItemExcelColumn.values().length; i++) {
            sheet.setColumnWidth(i, COLUMN_WIDTH);
        }
    }

    /**
     * 헤더 셀 스타일을 생성한다.
     * 굵은 글씨 + 회색 배경(GREY_25_PERCENT) + 중앙 정렬 + 얇은 테두리
     */
    private CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 배경색 설정
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 정렬
        style.setAlignment(HorizontalAlignment.CENTER);

        // 테두리
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 굵은 글씨
        org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        style.setFont(boldFont);

        return style;
    }

    /**
     * 금액 셀 스타일을 생성한다.
     * #,##0 포맷을 적용해 천 단위 구분 기호가 표시된다.
     */
    private CellStyle createMoneyStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat(MONEY_FORMAT));
        return style;
    }

}
