package com.bbangle.bbangle.controller;

import com.bbangle.bbangle.wishListStore.controller.WishListStoreController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WishListStoreControllerTest {

    @Autowired
    MockMvc mockMvc;

    private static final Authentication AUTHENTICATION = new TestingAuthenticationToken(
            "test1@gmail.com",
            null,
            "ROLE_USER");

    @Autowired
    private WishListStoreController wishListStoreController;


    @BeforeEach
    public void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(wishListStoreController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private final String BEARER = "Bearer";
    private final String AUTHORIZATION = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiYmFuZ2xlYmJhbmdsZSIsImlhdCI6MTcxMTgxNTQwNiwiZXhwIjoxNzExODI2MjA2LCJpZCI6MTB9._eIbWeiRajOKQpUPU0GjKJP7OmCBT3sdD9tWy9hjCr8";

    @DisplayName("위시리스트 스토어 전체 조회를 시행한다 오프셋 기반 페이지 네이션")
    @Test
    public void getWishListStores() throws Exception{
        mockMvc.perform(get("/api/v1/likes/stores")
                .header("Authorization", String.format("%s %s",BEARER, AUTHORIZATION))
                .with(SecurityMockMvcRequestPostProcessors.authentication(AUTHENTICATION))
                .param("page", "0")
                .param("size", "1")
                .param("sort", "createdAt,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contents[0].introduce").value("건강을 먹다-로썸"))
                .andDo(print());
    }


    @DisplayName("위시리스트 스토어 전체 조회를 시행한다 커서 기반 페이지 네이션")
    @Test
    public void getWishListStoresByCursor() throws Exception{
        mockMvc.perform(get("/api/v1/likes/stores/cursor")
                        .header("Authorization", String.format("%s %s",BEARER, AUTHORIZATION))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(AUTHENTICATION))
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contents[0].introduce").value("건강을 먹다-로썸"))
                .andDo(print());
    }
}
