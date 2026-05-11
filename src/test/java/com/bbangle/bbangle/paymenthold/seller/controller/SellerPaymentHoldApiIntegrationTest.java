package com.bbangle.bbangle.paymenthold.seller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.client.annotation.WithMockAuthenticationPrincipal;
import com.bbangle.bbangle.exception.BbangleErrorCode;
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
@SpringBootTest(properties = {
    "link-tracking.destinations=http://localhost"
})
@AutoConfigureMockMvc
@Transactional
@Sql(statements = {
    "INSERT INTO sellers (id, created_at, is_deleted, name, provider, provider_id, status, store_id) " +
        "VALUES (9101, CURRENT_TIMESTAMP, FALSE, 'payholdtest', 'KAKAO', 'payment-hold-9101', 'APPROVED', NULL)",
    "INSERT INTO settlement_item " +
        "(id, settlement_number, type, scheduled_amount, base_date, scheduled_date, completed_date, status, daily_settlement_id, order_item_id, created_at, modified_at) VALUES " +
        "(6001, '260401A1F7', 'NORMAL', 25000.00, '2026-04-01', '2026-04-08', NULL, 'PENDING', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)," +
        "(6002, '260401A1F7', 'NORMAL', 31000.00, '2026-04-03', '2026-04-10', '2026-04-04', 'PENDING', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)," +
        "(6003, '260405C3M1', 'NORMAL', 42000.00, '2026-04-05', '2026-04-12', NULL, 'PENDING', NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "INSERT INTO payment_hold " +
        "(id, seller_id, settlement_item_id, settlement_number, status, base_date, completed_date, settlement_amount, created_at, modified_at) VALUES " +
        "(1, 9101, 6001, '260401A1F7', 'ON_HOLD', '2026-04-01', NULL, 25000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)," +
        "(2, 9101, 6002, '260401A1F7', 'RELEASED', '2026-04-03', '2026-04-04', 31000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)," +
        "(3, 9101, 6003, '260405C3M1', 'ON_HOLD', '2026-04-05', NULL, 42000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"
})
@DisplayName("[통합테스트] SellerPaymentHold 조회/엑셀 API")
class SellerPaymentHoldApiIntegrationTest {

    private static final String BASE_URL = "/api/v1/seller/payment-hold";
    private static final String EXCEL_URL = "/api/v1/seller/payment-hold/excel";
    private static final String XLSX_CONTENT_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("지급보류 조회는 상태와 기간 필터를 적용한 페이지를 반환한다")
    void getPaymentHolds_returnsPagedData() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("dateType", "BASE_DATE")
                .param("startDate", "2026-04-01")
                .param("endDate", "2026-04-05")
                .param("status", "ON_HOLD")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.paymentHolds.content.length()").value(2))
            .andExpect(jsonPath("$.result.paymentHolds.content[0].paymentHoldId").value(3))
            .andExpect(jsonPath("$.result.paymentHolds.content[0].settlementId").value("260405C3M1"))
            .andExpect(jsonPath("$.result.paymentHolds.content[0].status").value("지급보류"))
            .andExpect(jsonPath("$.result.paymentHolds.totalElements").value(2));
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("정산 완료일 기준 조회가 가능하다")
    void getPaymentHolds_byCompletedDate_returnsFilteredData() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("dateType", "COMPLETED_DATE")
                .param("startDate", "2026-04-04")
                .param("endDate", "2026-04-04")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.paymentHolds.content.length()").value(1))
            .andExpect(jsonPath("$.result.paymentHolds.content[0].paymentHoldId").value(2))
            .andExpect(jsonPath("$.result.paymentHolds.content[0].status").value("해제"));
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("정산 ID 검색은 기간과 무관하게 관련 지급보류를 반환한다")
    void getPaymentHolds_bySettlementId_ignoresDateRange() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-01-31")
                .param("settlementId", "260401A1F7")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.paymentHolds.content.length()").value(2))
            .andExpect(jsonPath("$.result.paymentHolds.content[0].paymentHoldId").value(2));
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("기간 조회는 최대 1개월까지만 허용한다")
    void getPaymentHolds_dateRangeOverOneMonth_returnsBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("dateType", "BASE_DATE")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-02-02"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(BbangleErrorCode.PAYMENT_HOLD_DATE_RANGE_EXCEEDED.getCode()));
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("시작일이 종료일보다 늦으면 400을 반환한다")
    void getPaymentHolds_startDateAfterEndDate_returnsBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("dateType", "BASE_DATE")
                .param("startDate", "2026-04-05")
                .param("endDate", "2026-04-01"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(BbangleErrorCode.INVALID_PAYMENT_HOLD_DATE_RANGE.getCode()));
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("지급보류 엑셀 다운로드는 한글 헤더와 정산금액이 포함된 워크북을 반환한다")
    void downloadPaymentHoldExcel_returnsWorkbook() throws Exception {
        MvcResult result = mockMvc.perform(get(EXCEL_URL)
                .param("settlementId", "260401A1F7"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, XLSX_CONTENT_TYPE))
            .andExpect(header().string(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"payment-hold_260401A1F7.xlsx\""
            ))
            .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertThat(content).isNotEmpty();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheet("지급보류 내역");

            assertThat(sheet).isNotNull();
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(3);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("지급보류 ID");
            assertThat(sheet.getRow(0).getCell(5).getStringCellValue()).isEqualTo("정산 금액");
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("2");
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("260401A1F7");
            assertThat(sheet.getRow(1).getCell(5).getNumericCellValue()).isEqualTo(31000.00);
        }
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("엑셀 날짜 조회는 시작일과 종료일이 모두 필요하다")
    void downloadPaymentHoldExcel_withoutDates_returnsBadRequest() throws Exception {
        mockMvc.perform(get(EXCEL_URL))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(BbangleErrorCode.PAYMENT_HOLD_DATE_REQUIRED.getCode()));
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("엑셀은 지급보류 ID 검색이면 날짜 없이도 다운로드할 수 있다")
    void downloadPaymentHoldExcel_byPaymentHoldId_withoutDates_returnsOk() throws Exception {
        mockMvc.perform(get(EXCEL_URL)
                .param("paymentHoldId", "1"))
            .andExpect(status().isOk())
            .andExpect(header().string(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"payment-hold_1.xlsx\""
            ));
    }
}
