package com.bbangle.bbangle.board.controller;


import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.dto.ProductResponse;
import com.bbangle.bbangle.board.service.ProductService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

class ProeuctControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    @WithMockUser
    void testGetProductResponseWithMemberId() throws Exception {
        Long memberId = 1L;

        ProductResponse productResponse = ProductResponse.builder()
            .products(List.of())
            .boardIsBundled(true)
            .isSoldOut(true)
            .build();

        // Mocking 서비스 메서드 호출
        when(productService.getProductResponseWithPush(anyLong(), anyLong())).thenReturn(productResponse);

        // GET 요청 수행 및 검증
        mockMvc.perform(get("/api/v1/boards/1/product")
                .principal(() -> memberId.toString())) // 인증된 사용자 설정
            .andExpect(status().isOk());
    }

    @Test
    void testGetProductResponseWithoutMemberId() throws Exception {

        ProductResponse productResponse = ProductResponse.builder()
            .products(List.of())
            .boardIsBundled(true)
            .isSoldOut(true)
            .build();

        // Mocking 서비스 메서드 호출
        when(productService.getProductResponse(anyLong())).thenReturn(productResponse);

        // GET 요청 수행 및 검증
        mockMvc.perform(get("/api/v1/boards/1/product"))
            .andExpect(status().isOk());
    }

}


