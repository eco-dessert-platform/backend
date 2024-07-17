package com.bbangle.bbangle.member.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.preference.service.PreferenceService;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import java.time.Duration;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberControllerTest extends AbstractIntegrationTest {

    @Autowired
    PreferenceService preferenceService;

    @Autowired
    TokenProvider tokenProvider;

    Member member;

    @BeforeEach
    void setup() {
        member = memberService.getFirstJoinedMember(MemberFixture.createKakaoMember());
    }

    @Test
    @DisplayName("정상적으로 동의서와 선호도 작성 여부를 받아볼 수 있다.")
    void getIsAssignedApi() throws Exception {
        //given
        String authentication = getAuthentication(member);

        //when, then
        mockMvc.perform(get("/api/v1/members/status")
                .header(HttpHeaders.AUTHORIZATION, authentication))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.message").value("SUCCESS"))
            .andExpect(jsonPath("$.result.isFullyAssigned").value(false))
            .andExpect(jsonPath("$.result.isPreferenceAssigned").value(false));

    }

    private String getAuthentication(Member member) {
        String token = tokenProvider.generateToken(member, Duration.ofMinutes(1));
        return "Bearer " + token;
    }
}
