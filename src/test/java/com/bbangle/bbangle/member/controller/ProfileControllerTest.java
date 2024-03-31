package com.bbangle.bbangle.member.controller;


import com.bbangle.bbangle.member.controller.ProfileController;
import com.bbangle.bbangle.member.repository.ProfileRepository;
import com.bbangle.bbangle.member.service.ProfileServiceImpl;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProfileControllerTest {

    @Autowired
    ProfileServiceImpl profileService;
    @Autowired
    ProfileRepository profileRepository;
    @Autowired
    MockMvc mockMvc;

    @Before
    public void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new ProfileController(profileService)).build();
    }

    private final String BEARER = "Bearer";
    private final String AUTHORIZATION = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiYmFuZ2xlYmJhbmdsZSIsImlhdCI6MTcwOTM5MDUwOSwiZXhwIjoxNzA5NDAxMzA5LCJpZCI6MTB9.CjmhZpxDVa2QTsUpQBwxOo8QCoF31uK8SIzlK9EgWVA";
    @DisplayName("닉네임 중복 검사를 시행한다")
    @ParameterizedTest(name = "{index} : {0}")
    @ValueSource(strings = {"test"})
    public void checkNickname(String nickname) throws Exception {
        //given
        mockMvc.perform(get("/api/v1/profile/doublecheck")
                        .header("Authorization", String.format("%s %s",BEARER, AUTHORIZATION))
                        .param("nickname", nickname))
                        .andExpect(status().isOk())
                        .andDo(print());
    }

    @DisplayName("닉네임이 20자 이하이거나 비어 있는지 확인한다")
    @ParameterizedTest
    @ValueSource(strings = {"thisnicknameisexceed20characterright", " ", "", "\t", "\n"})
    public void isExceed20Character(String nickname) throws Exception{
        //given
        mockMvc.perform(get("/api/v1/profile/doublecheck")
                        .header("Authorization", String.format("%s %s", BEARER, AUTHORIZATION))
                        .param("nickname", nickname))
                        .andExpect(status().isBadRequest())
                        .andDo(print());

    }

    @Test
    @DisplayName("프로필 조회")
    public void getProfile() throws Exception{
        //given
        ResultActions result = mockMvc.perform(get("/api/v1/profile")
                .header("Authorization", String.format("%s %s", BEARER, AUTHORIZATION)));
        //when

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("윤동석"));
    }

}

