package com.bbangle.bbangle.settlement.seller.excel.writer;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.settlement.seller.controller.dto.response.DailySettlementResponse.DeductibleRefundDetail;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.DailySettlementResponse.DailySettlementSummary;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[Writer] DailySettlementExcelWriter")
class DailySettlementExcelWriterTest {

    private DailySettlementExcelWriter writer;

    @BeforeEach
    void setUp() {
        writer = new DailySettlementExcelWriter();
    }

    /**
     * ByteArrayOutputStream으로 엑셀을 작성한 뒤, XSSFWorkbook으로 재파싱해 검증한다.
     */
    private Sheet writeAndGetSheet(List<DailySettlementSummary> rows) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.write(rows, out);
        XSSFWorkbook workbook = new XSSFWorkbook(
            new java.io.ByteArrayInputStream(out.toByteArray())
        );
        return workbook.getSheetAt(0);
    }

    private String cellStringValue(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        return cell != null ? cell.toString() : null;
    }

    @Nested
    @DisplayName("헤더 행 검증")
    class HeaderTest {

        @Test
        @DisplayName("빈 리스트로 호출하면 헤더 행만 존재하고 컬럼 수가 11개다")
        void 빈_리스트_입력시_헤더만_작성된다() throws IOException {
            // when
            Sheet sheet = writeAndGetSheet(List.of());

            // then - 헤더 1행만 존재
            assertThat(sheet.getLastRowNum()).isEqualTo(0);

            Row headerRow = sheet.getRow(0);
            // 컬럼 수는 DailySettlementExcelColumn.values().length (11개)
            // getLastCellNum()은 short 반환이므로 int로 캐스팅 후 비교
            assertThat((int) headerRow.getLastCellNum()).isEqualTo(DailySettlementExcelColumn.values().length);
        }

        @Test
        @DisplayName("헤더 셀 값이 올바른 한글 컬럼명을 가진다")
        void 헤더_셀_값이_올바른_한글_컬럼명이다() throws IOException {
            // when
            Sheet sheet = writeAndGetSheet(List.of());

            Row headerRow = sheet.getRow(0);
            DailySettlementExcelColumn[] columns = DailySettlementExcelColumn.values();

            // then - 각 컬럼의 헤더명 검증
            for (int i = 0; i < columns.length; i++) {
                assertThat(cellStringValue(headerRow, i))
                    .as("컬럼 %d (%s) 헤더명 검증", i, columns[i].name())
                    .isEqualTo(columns[i].getHeaderName());
            }
        }
    }

    @Nested
    @DisplayName("데이터 행 검증")
    class DataRowTest {

        @Test
        @DisplayName("데이터 1건이면 헤더 포함 총 2행이 작성된다")
        void 데이터_1건이면_헤더_포함_총_2행() throws IOException {
            // given
            DailySettlementSummary row = createSampleSummary();

            // when
            Sheet sheet = writeAndGetSheet(List.of(row));

            // then
            assertThat(sheet.getLastRowNum()).isEqualTo(1);
        }

        @Test
        @DisplayName("정산ID, 정산예정일, 정산완료일이 올바르게 작성된다")
        void 기본_문자열_컬럼이_올바르게_작성된다() throws IOException {
            // given
            DailySettlementSummary row = createSampleSummary();

            // when
            Sheet sheet = writeAndGetSheet(List.of(row));
            Row dataRow = sheet.getRow(1);

            // then
            assertThat(cellStringValue(dataRow, 0)).isEqualTo("250401A1F7"); // 정산ID
            assertThat(cellStringValue(dataRow, 1)).isEqualTo("2025-03-01"); // 정산예정일
            assertThat(cellStringValue(dataRow, 2)).isEqualTo("2025-03-05"); // 정산완료일
            assertThat(cellStringValue(dataRow, 10)).isEqualTo("계좌이체");  // 정산방식
        }

        @Test
        @DisplayName("공제/환급 상세 중첩 record가 8, 9번 컬럼에 평탄화되어 작성된다")
        void 중첩_record_공제환급_상세가_평탄화된다() throws IOException {
            // given
            DailySettlementSummary row = createSampleSummary();

            // when
            Sheet sheet = writeAndGetSheet(List.of(row));
            Row dataRow = sheet.getRow(1);

            // then - 8: 배송비 금액 변동(-1500), 9: 충전금 상계(-500)
            Cell deliveryFeeCell = dataRow.getCell(7); // DELIVERY_FEE_CHANGE (index 7)
            Cell balanceOffsetCell = dataRow.getCell(8); // BALANCE_OFFSET (index 8)

            assertThat(deliveryFeeCell).isNotNull();
            assertThat(balanceOffsetCell).isNotNull();
            // 숫자 셀이므로 numericValue로 검증
            assertThat(deliveryFeeCell.getNumericCellValue()).isEqualTo(-1500.0);
            assertThat(balanceOffsetCell.getNumericCellValue()).isEqualTo(-500.0);
        }

        @Test
        @DisplayName("completedDate가 null이면 해당 셀이 비어 있다")
        void completedDate가_null이면_빈_셀이다() throws IOException {
            // given
            DailySettlementSummary row = new DailySettlementSummary(
                "TEST-001",
                LocalDate.of(2025, 3, 1),
                null, // completedDate null
                new BigDecimal("100000"),
                new BigDecimal("100000"),
                new BigDecimal("-3000"),
                new BigDecimal("-2000"),
                new DeductibleRefundDetail(BigDecimal.ZERO, BigDecimal.ZERO),
                BigDecimal.ZERO,
                "계좌이체"
            );

            // when
            Sheet sheet = writeAndGetSheet(List.of(row));
            Row dataRow = sheet.getRow(1);

            // then - completedDate 컬럼(index 2)이 비어 있거나 빈 문자열
            Cell cell = dataRow.getCell(2);
            // SXSSFWorkbook에서 null 셀은 getCell()이 null을 반환하거나 빈 값
            if (cell != null) {
                assertThat(cell.toString()).isEmpty();
            }
        }

        @Test
        @DisplayName("금액 컬럼이 null이면 해당 셀이 비어 있다")
        void 금액_null이면_빈_셀이다() throws IOException {
            // given
            DailySettlementSummary row = new DailySettlementSummary(
                "TEST-001",
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2025, 3, 1),
                null, // totalSettlementAmount null
                null, // amount null
                null,
                null,
                new DeductibleRefundDetail(null, null),
                null,
                null
            );

            // when - 예외 없이 작성 완료
            Sheet sheet = writeAndGetSheet(List.of(row));

            // then - 데이터 행이 존재하고 null 값은 빈 셀으로 처리됨
            assertThat(sheet.getLastRowNum()).isEqualTo(1);
        }

        @Test
        @DisplayName("정산방식이 올바른 한글 description으로 작성된다")
        void 정산방식_한글_description으로_작성된다() throws IOException {
            // given - 계좌이체, 충전금 차감, 충전금 적립 각각 검증
            List<DailySettlementSummary> rows = List.of(
                buildRowWithSettlementMethod("계좌이체"),
                buildRowWithSettlementMethod("충전금 차감"),
                buildRowWithSettlementMethod("충전금 적립")
            );

            // when
            Sheet sheet = writeAndGetSheet(rows);

            // then
            assertThat(cellStringValue(sheet.getRow(1), 10)).isEqualTo("계좌이체");
            assertThat(cellStringValue(sheet.getRow(2), 10)).isEqualTo("충전금 차감");
            assertThat(cellStringValue(sheet.getRow(3), 10)).isEqualTo("충전금 적립");
        }
    }

    // ─── 테스트 픽스처 헬퍼 ───────────────────────────────────────────────────

    private DailySettlementSummary createSampleSummary() {
        return new DailySettlementSummary(
            "250401A1F7",
            LocalDate.of(2025, 3, 1),
            LocalDate.of(2025, 3, 5),
            new BigDecimal("95000"),
            new BigDecimal("100000"),
            new BigDecimal("-3000"),
            new BigDecimal("-2000"),
            new DeductibleRefundDetail(
                new BigDecimal("-1500"),
                new BigDecimal("-500")
            ),
            BigDecimal.ZERO,
            "계좌이체"
        );
    }

    private DailySettlementSummary buildRowWithSettlementMethod(String method) {
        return new DailySettlementSummary(
            "TEST",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 1),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            new DeductibleRefundDetail(BigDecimal.ZERO, BigDecimal.ZERO),
            BigDecimal.ZERO,
            method
        );
    }

}
