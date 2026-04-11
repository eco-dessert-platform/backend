package com.bbangle.bbangle.settlement.seller.excel.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.client.annotation.WithMockAuthenticationPrincipal;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.settlement.seller.excel.service.SellerSettlementExcelService;
import java.io.OutputStream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("[컨트롤러 테스트] SellerSettlementExcelController")
@WebMvcTest(controllers = SellerSettlementExcelController.class)
@Import({
    TestSlackAdaptorConfig.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@ActiveProfiles("test")
class SellerSettlementExcelControllerTest {

    private static final String DOWNLOAD_URL = SellerApiPath.PREFIX + "/settlements/daily/excel";
    private static final String XLSX_CONTENT_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SellerSettlementExcelService excelService;

    @Nested
    @DisplayName("GET /settlements/daily/excel 다운로드 테스트")
    class DownloadDailySettlementExcelTest {

        // void 메서드이므로 Mockito 기본 동작(아무것도 안 함)과 동일하지만,
        // 테스트 의도를 명시적으로 드러내기 위해 setup을 공통화한다.
        // lenient(): 401/403/400 테스트처럼 컨트롤러 진입 전에 차단되는 경우 stub이 사용되지
        // 않아 strict 모드에서 UnnecessaryStubbingException이 발생하므로 lenient로 설정
        @BeforeEach
        void setUp() throws Exception {
            lenient().doNothing().when(excelService).writeDailySettlementExcel(any(), any());
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("날짜 조건 없이 요청하면 400 에러를 반환한다 (엑셀은 날짜 범위 필수)")
        void 날짜_없이_요청하면_400() throws Exception {
            mockMvc.perform(get(DOWNLOAD_URL))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("startDate만 지정하면 400 에러를 반환한다 (endDate 미입력)")
        void startDate만_지정하면_400() throws Exception {
            mockMvc.perform(get(DOWNLOAD_URL)
                    .param("startDate", "2025-03-01"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("endDate만 지정하면 400 에러를 반환한다 (startDate 미입력)")
        void endDate만_지정하면_400() throws Exception {
            mockMvc.perform(get(DOWNLOAD_URL)
                    .param("endDate", "2025-03-31"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("1개월을 초과하는 날짜 범위로 요청하면 400 에러를 반환한다")
        void 날짜_범위_1개월_초과시_400() throws Exception {
            mockMvc.perform(get(DOWNLOAD_URL)
                    .param("startDate", "2025-01-01")
                    .param("endDate", "2025-02-02"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("startDate가 endDate보다 이후이면 400 에러를 반환한다")
        void startDate가_endDate보다_이후이면_400() throws Exception {
            mockMvc.perform(get(DOWNLOAD_URL)
                    .param("startDate", "2025-04-01")
                    .param("endDate", "2025-03-01"))
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("정확히 1개월 범위(startDate ~ startDate+1개월)로 요청하면 200 응답을 반환한다")
        void 정확히_1개월_범위는_200() throws Exception {
            mockMvc.perform(get(DOWNLOAD_URL)
                    .param("startDate", "2025-01-01")
                    .param("endDate", "2025-02-01"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, XLSX_CONTENT_TYPE));
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("날짜 조건을 지정하면 Content-Disposition 파일명에 날짜가 포함된다")
        void 날짜_지정시_파일명에_날짜가_포함된다() throws Exception {
            mockMvc.perform(get(DOWNLOAD_URL)
                    .param("startDate", "2025-03-01")
                    .param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"daily-settlements_2025-03-01_2025-03-31.xlsx\""
                ));
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("정상 요청에 Cache-Control 헤더가 no-cache로 설정된다")
        void 정상_요청시_Cache_Control_설정() throws Exception {
            mockMvc.perform(get(DOWNLOAD_URL)
                    .param("startDate", "2025-03-01")
                    .param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate"));
        }

        @Test
        @DisplayName("인증되지 않은 요청은 401 에러를 반환한다")
        void 미인증_요청은_401_에러() throws Exception {
            // SecurityConfig에서 /api/** 경로의 미인증 요청은 401 UNAUTHORIZED를 반환한다
            mockMvc.perform(get(DOWNLOAD_URL))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "CUSTOMER")
        @DisplayName("판매자가 아닌 역할(CUSTOMER)로 요청하면 403 에러를 반환한다")
        void 고객_역할로_요청하면_403() throws Exception {
            // 판매자 전용 API이므로 CUSTOMER 역할은 접근 불가
            mockMvc.perform(get(DOWNLOAD_URL))
                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("응답 바이트가 XLSX 시그니처(PK 매직 바이트)로 시작한다")
        void 응답_바이트가_XLSX_시그니처로_시작한다() throws Exception {
            // given - 서비스가 실제 최소 XLSX 바이트를 스트림에 기록
            willAnswer(invocation -> {
                OutputStream outputStream = invocation.getArgument(1);
                // try-with-resources로 XSSFWorkbook 리소스 누수를 방지
                try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                    workbook.createSheet("정산내역");
                    workbook.write(outputStream);
                }
                return null;
            }).given(excelService).writeDailySettlementExcel(any(), any());

            // when
            byte[] responseBytes = mockMvc.perform(get(DOWNLOAD_URL)
                    .param("startDate", "2025-03-01")
                    .param("endDate", "2025-03-31"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

            // then - XLSX는 ZIP 기반 포맷이므로 PK 매직 바이트(0x50=P, 0x4B=K)로 시작한다
            assertThat(responseBytes).hasSizeGreaterThan(2);
            assertThat(responseBytes[0]).isEqualTo((byte) 0x50); // 'P'
            assertThat(responseBytes[1]).isEqualTo((byte) 0x4B); // 'K'
        }
    }

}
