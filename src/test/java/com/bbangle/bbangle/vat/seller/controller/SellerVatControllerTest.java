package com.bbangle.bbangle.vat.seller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.vat.seller.controller.dto.response.SellerVatMonthlyItem;
import com.bbangle.bbangle.vat.seller.controller.dto.response.SellerVatSummaryResponse;
import com.bbangle.bbangle.vat.seller.service.SellerVatService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("[Controller] SellerVatController")
class SellerVatControllerTest {

    @Mock
    private SellerVatService sellerVatService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SellerVatController controller = new SellerVatController(
            new ResponseService(null),
            sellerVatService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .build();
    }

    @Test
    @DisplayName("부가세 신고 내역 조회 API는 월별 합산 응답을 SingleResult로 반환한다")
    void getSellerVatSummary_returnsSingleResult() throws Exception {
        // given
        given(sellerVatService.getVatSummary(any()))
            .willReturn(new SellerVatSummaryResponse(
                "2025-03",
                "2025-04",
                List.of(new SellerVatMonthlyItem(
                    "2025-04",
                    1875000L,
                    320000L,
                    1250000L,
                    430000L,
                    260000L,
                    255000L
                ))
            ));

        // when & then
        mockMvc.perform(get("/api/v1/seller/vat")
                .param("startMonth", "2025-03")
                .param("endMonth", "2025-04"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.result.startMonth").value("2025-03"))
            .andExpect(jsonPath("$.result.endMonth").value("2025-04"))
            .andExpect(jsonPath("$.result.items[0].month").value("2025-04"))
            .andExpect(jsonPath("$.result.items[0].taxableSalesAmount").value(1875000));

        verify(sellerVatService).getVatSummary(argThat(command ->
            command.sellerId().equals(1L)
                && command.startMonth().toString().equals("2025-03")
                && command.endMonth().toString().equals("2025-04")
        ));
    }
}
