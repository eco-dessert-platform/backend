package com.bbangle.bbangle.settlement.seller.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.client.annotation.WithMockAuthenticationPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 건별 정산 내역 조회 API 통합 테스트.
 * SQL로 seller → daily_settlement → settlement_item → order_item → orders → product 계층을 세팅한다.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(statements = {
    // 판매자 등록
    "INSERT INTO sellers (id, created_at, is_deleted, name, provider, provider_id, status, store_id) " +
        "VALUES (9101, CURRENT_TIMESTAMP, FALSE, 'item_settle', 'KAKAO', 'item-settle-test-9101', 'APPROVED', NULL)",

    // 일별 정산 등록
    "INSERT INTO daily_settlement " +
        "(id, scheduled_date, completed_date, amount, fee, deductible_refund, with_holding_payment, " +
        "settlement_method, created_at, modified_at, seller_id, settlement_number, delivery_fee_change, balance_offset) " +
        "VALUES " +
        "(2001, DATE '2026-04-01', DATE '2026-04-02', 100000.00, -3000.00, -2000.00, 0.00, " +
        "'BANK_TRANSFER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9101, 'DS-2001', -1000.00, -1000.00)," +
        "(2002, DATE '2026-04-05', NULL, 80000.00, -2400.00, 0.00, 0.00, " +
        "'BANK_TRANSFER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 9101, 'DS-2002', 0.00, 0.00)",

    // 주문 등록 (seller_id 포함)
    "INSERT INTO orders (id, order_number, order_group_id, order_date, buyer_name, buyer_phone, delivery_fee, total_amount, seller_id, created_at, modified_at) " +
        "VALUES " +
        "(3001, 'ORD-20260401-001', 'settle-test-grp-3001000000', CURRENT_TIMESTAMP, '홍길동', '01011111111', 2500, 50000, 9101, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)," +
        "(3002, 'ORD-20260405-002', 'settle-test-grp-3002000000', CURRENT_TIMESTAMP, '김철수', '01022222222', 2500, 30000, 9101, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",

    // 상품 등록 (is_deleted, stock 포함)
    "INSERT INTO product (id, title, price, is_soldout, is_deleted, stock, monday, tuesday, wednesday, thursday, friday, saturday, sunday, created_at, modified_at) " +
        "VALUES " +
        "(4001, '글루텐프리 식빵', 25000, FALSE, FALSE, 100, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)," +
        "(4002, '저당 크래커', 15000, FALSE, FALSE, 100, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",

    // 주문 항목 등록
    "INSERT INTO order_item (id, quantity, product_price, unit_price, order_status, delivery_status, total_price, order_id, product_id, created_at, modified_at) " +
        "VALUES " +
        "(5001, 2, 25000, 25000, 'PAYMENT_COMPLETED', 'PREPARING', 50000, 3001, 4001, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)," +
        "(5002, 1, 15000, 15000, 'PAYMENT_COMPLETED', 'PREPARING', 15000, 3002, 4002, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",

    // 건별 정산 등록 (status는 VARCHAR(20) 제한으로 PENDING, CANCELLED 사용 - 실제 앱에서는 Flyway V47+ 로 크기 확장 필요)
    "INSERT INTO settlement_item " +
        "(id, settlement_number, type, scheduled_amount, base_date, scheduled_date, completed_date, status, daily_settlement_id, order_item_id, created_at, modified_at) " +
        "VALUES " +
        "(6001, 'SI-001', 'NORMAL', 25000.00, DATE '2026-04-01', DATE '2026-04-08', NULL, 'PENDING', 2001, 5001, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)," +
        "(6002, 'SI-002', 'NORMAL', 15000.00, DATE '2026-04-05', DATE '2026-04-12', NULL, 'PENDING', 2002, 5002, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)," +
        "(6003, 'SI-003', 'NORMAL', 10000.00, DATE '2026-04-01', DATE '2026-04-08', DATE '2026-04-08', 'PENDING', 2001, 5001, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"
})
@DisplayName("[통합테스트] SellerSettlementItem 조회 API")
class SellerSettlementItemControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/seller/settlements/items";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("건별 정산 조회 - SQL로 넣은 데이터가 페이지와 요약 정보로 반환된다")
    void getSettlementItems_returnsPagedDataAndSummary() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.settlements.content.length()").value(3))
            .andExpect(jsonPath("$.result.settlements.page").value(0))
            .andExpect(jsonPath("$.result.settlements.size").value(10))
            .andExpect(jsonPath("$.result.settlements.totalElements").value(3))
            .andExpect(jsonPath("$.result.summary.totalCount").value(3));
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("건별 정산 조회 - id 내림차순으로 최신 데이터가 먼저 반환된다")
    void getSettlementItems_returnedInDescOrder() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,desc"))
            .andExpect(status().isOk())
            // id 6003이 먼저, 6001이 마지막
            .andExpect(jsonPath("$.result.settlements.content[0].orderItemId").value(5001));
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("baseDate 필터로 조회하면 해당 기간의 데이터만 반환된다")
    void getSettlementItems_withDateFilter_returnsFilteredData() throws Exception {
        // 2026-04-01 ~ 2026-04-01: settlement_item id=6001, id=6003 (baseDate=2026-04-01 2건)
        mockMvc.perform(get(BASE_URL)
                .param("startDate", "2026-04-01")
                .param("endDate", "2026-04-01")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.settlements.content.length()").value(2))
            .andExpect(jsonPath("$.result.settlements.totalElements").value(2))
            .andExpect(jsonPath("$.result.summary.totalCount").value(2));
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("정산 상태가 한글 설명으로 변환되어 반환된다")
    void getSettlementItems_statusReturnedAsDescription() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,desc"))
            .andExpect(status().isOk())
            // 모든 항목이 PENDING → "정산대기"
            .andExpect(jsonPath("$.result.settlements.content[0].status").value("정산대기"));
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("구매자 이름과 상품명이 조인을 통해 올바르게 반환된다")
    void getSettlementItems_joinDataReturnedCorrectly() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "id,desc"))
            .andExpect(status().isOk())
            // id=6003 (order_id=3001, buyer_name='홍길동', product='글루텐프리 식빵')
            .andExpect(jsonPath("$.result.settlements.content[0].buyerName").value("홍길동"))
            .andExpect(jsonPath("$.result.settlements.content[0].productTitle").value("글루텐프리 식빵"));
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "SELLER")
    @DisplayName("startDate가 endDate보다 이후이면 400 에러를 반환한다")
    void getSettlementItems_invalidDateRange_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("startDate", "2026-04-30")
                .param("endDate", "2026-04-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("미인증 요청은 401 에러를 반환한다")
    void getSettlementItems_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockAuthenticationPrincipal(userId = 9101L, role = "CUSTOMER")
    @DisplayName("판매자가 아닌 역할(CUSTOMER)로 요청하면 403 에러를 반환한다")
    void getSettlementItems_customerRole_returns403() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isForbidden());
    }

}
