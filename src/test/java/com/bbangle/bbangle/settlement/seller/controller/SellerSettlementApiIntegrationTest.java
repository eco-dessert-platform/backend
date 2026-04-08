package com.bbangle.bbangle.settlement.seller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.client.annotation.WithMockAuthenticationPrincipal;
import java.io.ByteArrayInputStream;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(statements = {
    "INSERT INTO sellers (id, created_at, is_deleted, name, provider, provider_id, status, store_id) " +
        "VALUES (9001, CURRENT_TIMESTAMP, FALSE, 'settle_test', 'KAKAO', 'settle-test-9001', 'APPROVED', NULL)",
    "INSERT INTO daily_settlement " +
        "(id, scheduled_date, completed_date, amount, fee, deductible_refund, with_holding_payment, " +
        "settlement_method, created_at, modified_at, seller_id, settlement_number, delivery_fee_change, balance_offset) " +
        "VALUES " +
        "(101, DATE '2026-04-01', DATE '2026-04-02', 100000.00, -3000.00, -2000.00, 0.00, " +
        "'BANK_TRANSFER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9001, '260401A1F7', -1000.00, -1000.00)," +
        "(102, DATE '2026-04-03', DATE '2026-04-04', 150000.00, -4500.00, 500.00, 0.00, " +
        "'BANK_TRANSFER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9001, '260403B2K9', 1000.00, -500.00)," +
        "(103, DATE '2026-04-05', NULL, 200000.00, -6000.00, -1500.00, -10000.00, " +
        "'BANK_TRANSFER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9001, '260405C3M1', -500.00, -1000.00)," +
        "(104, DATE '2026-04-07', NULL, 80000.00, -2400.00, 0.00, 0.00, " +
        "'BANK_TRANSFER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9001, '260407D4P2', 0.00, 0.00)"
})
@DisplayName("[통합테스트] SellerSettlement 조회/엑셀 API")
class SellerSettlementApiIntegrationTest {

    private static final String BASE_URL = "/api/v1/seller/settlements/daily";
    private static final String EXCEL_URL = "/api/v1/seller/settlements/daily/excel";
    private static final String XLSX_CONTENT_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockAuthenticationPrincipal(userId = 9001L, role = "SELLER")
    @DisplayName("일별 정산 조회 - SQL로 넣은 데이터를 페이지와 요약 정보로 반환한다")
    void getDailySettlements_returnsPagedDataAndSummary() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("startDate", "2026-04-01")
                .param("endDate", "2026-04-05")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.settlements.content.length()").value(3))
            .andExpect(jsonPath("$.result.settlements.content[0].settlementNumber").value("260405C3M1"))
            .andExpect(jsonPath("$.result.settlements.content[0].settlementMethod").value("계좌이체"))
            .andExpect(jsonPath("$.result.settlements.page").value(0))
            .andExpect(jsonPath("$.result.settlements.size").value(10))
            .andExpect(jsonPath("$.result.settlements.totalPages").value(1))
            .andExpect(jsonPath("$.result.settlements.totalElements").value(3))
            .andExpect(jsonPath("$.result.summary.scheduledDateMin").value("2026-04-01"))
            .andExpect(jsonPath("$.result.summary.scheduledDateMax").value("2026-04-05"))
            .andExpect(jsonPath("$.result.summary.totalSettlementAmount").value(423500.00));
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9001L, role = "SELLER")
    @DisplayName("일별 정산 엑셀 다운로드 - SQL로 넣은 데이터가 워크북에 작성된다")
    void downloadDailySettlementExcel_returnsWorkbookWithInsertedRows() throws Exception {
        MvcResult result = mockMvc.perform(get(EXCEL_URL)
                .param("startDate", "2026-04-01")
                .param("endDate", "2026-04-05"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, XLSX_CONTENT_TYPE))
            .andExpect(header().string(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"daily-settlements_2026-04-01_2026-04-05.xlsx\""
            ))
            .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertThat(content).isNotEmpty();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheet("일별 정산 내역");

            assertThat(sheet).isNotNull();
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(4);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("정산ID");
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("260405C3M1");
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("2026-04-05");
            assertThat(sheet.getRow(1).getCell(3).getNumericCellValue()).isEqualTo(182500.00);
            assertThat(sheet.getRow(1).getCell(10).getStringCellValue()).isEqualTo("계좌이체");
        }
    }
}
