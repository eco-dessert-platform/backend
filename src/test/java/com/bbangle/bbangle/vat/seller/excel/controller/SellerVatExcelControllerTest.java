package com.bbangle.bbangle.vat.seller.excel.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.vat.seller.controller.dto.request.SellerVatExcelType;
import com.bbangle.bbangle.vat.seller.excel.service.SellerVatExcelService;
import com.bbangle.bbangle.vat.seller.service.model.SellerVatCommand.SellerVatSearchCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("[Controller] SellerVatExcelController")
class SellerVatExcelControllerTest {

    private static final String XLSX_CONTENT_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Mock
    private SellerVatExcelService sellerVatExcelService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SellerVatExcelController controller = new SellerVatExcelController(sellerVatExcelService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .build();
    }

    @Test
    @DisplayName("엑셀 다운로드 API는 파일 헤더를 설정하고 엑셀 서비스를 호출한다")
    void downloadSellerVatExcel_setsHeadersAndDelegatesToService() throws Exception {
        // given
        doAnswer(invocation -> {
            invocation.getArgument(2, java.io.OutputStream.class).write("xlsx".getBytes());
            return null;
        }).when(sellerVatExcelService)
            .writeExcel(any(SellerVatSearchCommand.class), eq(SellerVatExcelType.MONTHLY), any());

        // when & then
        mockMvc.perform(get("/api/v1/seller/vat/excel")
                .param("startMonth", "2025-03")
                .param("endMonth", "2025-04")
                .param("type", "MONTHLY"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, XLSX_CONTENT_TYPE))
            .andExpect(header().string(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"seller_vat_2025-03_2025-04_MONTHLY.xlsx\""
            ));

        verify(sellerVatExcelService).writeExcel(
            argThat(command ->
                command.sellerId().equals(1L)
                    && command.startMonth().toString().equals("2025-03")
                    && command.endMonth().toString().equals("2025-04")
            ),
            eq(SellerVatExcelType.MONTHLY),
            any()
        );
    }
}
