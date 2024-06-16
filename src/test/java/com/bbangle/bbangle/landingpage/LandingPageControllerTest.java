package com.bbangle.bbangle.landingpage;

import com.bbangle.bbangle.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LandingPageControllerTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }


    @Test
    @DisplayName("이메일 값이 비어있는지 확인한다")
    void notEmptyEmail() throws Exception {
        mockMvc.perform(post("/api/v1/landingpage")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"''\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이메일 값이 형식에 맞지 않은지 확인한다")
    void checkEmailForm() throws Exception {
        mockMvc.perform(post("/api/v1/landingpage")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"abcabc@abc\"}"))
            .andExpect(status().isBadRequest());
    }
}
