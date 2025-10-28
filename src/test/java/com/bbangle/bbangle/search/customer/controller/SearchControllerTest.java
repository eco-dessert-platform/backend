package com.bbangle.bbangle.search.customer.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.AbstractIntegrationTest;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class SearchControllerTest extends AbstractIntegrationTest {

    private static final Long MEMBER_ID = 2L;

    @Test
    @DisplayName("saveKeyword는 검색어를 저장할 수 있다")
    void saveKeyword() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap();
        params.put("keyword", Collections.singletonList("비건"));

        mockMvc.perform(post("/api/v1/search")
                .params(params)
                .with(user(String.valueOf(MEMBER_ID)).password("testPassword").roles("USER")))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("deleteRecencyKeyword 메서드는 최근 검색어를 삭제할 수 있다")
    void deleteRecencyKeyword() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap();
        params.put("keyword", Collections.singletonList("비건"));

        mockMvc.perform(delete("/api/v1/search/recency")
                .params(params)
                .with(user(String.valueOf(MEMBER_ID)).password("testPassword").roles("USER")))
            .andDo(print())
            .andExpect(status().isOk());
    }

}

