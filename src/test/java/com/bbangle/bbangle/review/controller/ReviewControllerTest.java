package com.bbangle.bbangle.review.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

class ReviewControllerTest extends AbstractIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void getSummaryReviewTest() throws Exception {
        mockMvc.perform(get("/api/v1/boards/1/review"))
            .andExpect(status().isOk())
            .andDo(print());
    }

}
