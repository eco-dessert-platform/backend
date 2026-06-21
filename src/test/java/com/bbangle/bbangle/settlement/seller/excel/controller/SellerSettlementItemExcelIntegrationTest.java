package com.bbangle.bbangle.settlement.seller.excel.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

/**
 * 건별 정산 내역 엑셀 다운로드 API 통합 테스트.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(statements = {
    // 판매자 등록
    "INSERT INTO sellers (id, created_at, is_deleted, name, provider, provider_id, status, store_id) " +
        "VALUES (9201, CURRENT_TIMESTAMP, FALSE, 'excel_item', 'KAKAO', 'excel-item-test-9201', 'APPROVED', NULL)",

    // 일별 정산 등록
    "INSERT INTO daily_settlement " +
        "(id, scheduled_date, completed_date, amount, fee, deductible_refund, with_holding_payment, " +
        "settlement_method, created_at, modified_at, seller_id, settlement_number, delivery_fee_change, balance_offset) " +
        "VALUES " +
        "(7001, DATE '2026-04-01', DATE '2026-04-02', 100000.00, -3000.00, -2000.00, 0.00, " +
        "'BANK_TRANSFER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9201, 'DS-7001', -1000.00, -1000.00)",

    // 주문 등록
    "INSERT INTO orders (id, order_number, order_group_id, order_date, buyer_name, buyer_phone, delivery_fee, total_amount, seller_id, created_at, modified_at) " +
        "VALUES " +
        "(8001, 'ORD-20260401-EXCEL', 'settle-test-grp-8001000000', CURRENT_TIMESTAMP, '엑셀테스터', '01099999999', 2500, 50000, 9201, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",

    // 상품 등록 (is_deleted, stock 포함)
    "INSERT INTO product (id, title, price, is_soldout, is_deleted, stock, monday, tuesday, wednesday, thursday, friday, saturday, sunday, created_at, modified_at) " +
        "VALUES " +
        "(9001, '엑셀테스트 상품', 25000, FALSE, FALSE, 100, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",

    // 주문 항목 등록
    "INSERT INTO order_item (id, quantity, product_price, unit_price, order_status, delivery_status, total_price, order_id, product_id, created_at, modified_at) " +
        "VALUES " +
        "(10001, 2, 25000, 25000, 'PAYMENT_COMPLETED', 'PREPARING', 50000, 8001, 9001, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",

    // 건별 정산 등록
    "INSERT INTO settlement_item " +
        "(id, settlement_number, type, scheduled_amount, base_date, scheduled_date, completed_date, status, daily_settlement_id, order_item_id, created_at, modified_at) " +
        "VALUES " +
        "(11001, 'SI-EXCEL-001', 'NORMAL', 25000.00, DATE '2026-04-01', DATE '2026-04-08', NULL, 'PENDING', 7001, 10001, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)," +
        "(11002, 'SI-EXCEL-002', 'NORMAL', 15000.00, DATE '2026-04-01', DATE '2026-04-08', NULL, 'PENDING', 7001, 10001, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"
})
@DisplayName("[통합테스트] SellerSettlementItem 엑셀 다운로드 API")
class SellerSettlementItemExcelIntegrationTest {

    private static final String EXCEL_URL = "/api/v1/seller/settlements/items/excel";
    private static final String XLSX_CONTENT_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockAuthenticationPrincipal(userId = 9201L, role = "SELLER")
    @DisplayName("엑셀 다운로드 - 정상 요청 시 XLSX 응답이 반환된다")
    void downloadSettlementItemExcel_returnsXlsx() throws Exception {
        MvcResult result = mockMvc.perform(get(EXCEL_URL)
                .param("startDate", "2026-04-01")
                .param("endDate", "2026-04-30"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, XLSX_CONTENT_TYPE))
            .andExpect(header().string(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"settlement-items_2026-04-01_2026-04-30.xlsx\""
            ))
            .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        // 응답 바이트가 비어있지 않아야 한다
        assertThat(content).isNotEmpty();
        // XLSX는 ZIP 기반 포맷이므로 PK 매직 바이트(0x50=P, 0x4B=K)로 시작한다
        assertThat(content[0]).isEqualTo((byte) 0x50);
        assertThat(content[1]).isEqualTo((byte) 0x4B);
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9201L, role = "SELLER")
    @DisplayName("엑셀 다운로드 - 워크북에 정산 데이터가 정확히 기록된다")
    void downloadSettlementItemExcel_workbookContainsData() throws Exception {
        MvcResult result = mockMvc.perform(get(EXCEL_URL)
                .param("startDate", "2026-04-01")
                .param("endDate", "2026-04-30"))
            .andExpect(status().isOk())
            .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            Sheet sheet = workbook.getSheet("건별 정산 내역");

            // 시트가 존재해야 한다
            assertThat(sheet).isNotNull();
            // 헤더(1행) + 데이터(2건) = 총 3행
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(3);
            // 첫 번째 컬럼 헤더 확인
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("주문번호");
        }
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9201L, role = "SELLER")
    @DisplayName("엑셀 다운로드 - 날짜 없이 요청하면 400 에러를 반환한다")
    void downloadSettlementItemExcel_withoutDate_returns400() throws Exception {
        mockMvc.perform(get(EXCEL_URL))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9201L, role = "SELLER")
    @DisplayName("엑셀 다운로드 - 1개월 초과 기간으로 요청하면 400 에러를 반환한다")
    void downloadSettlementItemExcel_exceededDateRange_returns400() throws Exception {
        mockMvc.perform(get(EXCEL_URL)
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-02-02"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9201L, role = "SELLER")
    @DisplayName("엑셀 다운로드 - Cache-Control 헤더가 no-cache로 설정된다")
    void downloadSettlementItemExcel_cacheControlHeader() throws Exception {
        mockMvc.perform(get(EXCEL_URL)
                .param("startDate", "2026-04-01")
                .param("endDate", "2026-04-30"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"));
    }

    @Test
    @DisplayName("미인증 요청은 401 에러를 반환한다")
    void downloadSettlementItemExcel_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(EXCEL_URL)
                .param("startDate", "2026-04-01")
                .param("endDate", "2026-04-30"))
            .andExpect(status().isUnauthorized());
    }

}
