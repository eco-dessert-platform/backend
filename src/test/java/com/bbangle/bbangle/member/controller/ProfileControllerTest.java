package com.bbangle.bbangle.member.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.member.dto.ProfileInfoResponseDto;
import com.bbangle.bbangle.member.service.ProfileService;
import com.bbangle.bbangle.mock.WithCustomMockUser;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProfileControllerTest extends AbstractIntegrationTest {

    @MockBean
    private ProfileService profileService;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private MockMvc mockMvc;
    private final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
        .build();

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(
            new ProfileController(profileService, responseService)
        ).build();
    }

    private final String BEARER = "Bearer";
    private final String AUTHORIZATION = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiYmFuZ2xlYmJhbmdsZSIsImlhdCI6MTcwOTM5MDUwOSwiZXhwIjoxNzA5NDAxMzA5LCJpZCI6MTB9.CjmhZpxDVa2QTsUpQBwxOo8QCoF31uK8SIzlK9EgWVA";

    @DisplayName("닉네임 중복 검사를 시행한다")
    @ParameterizedTest(name = "{index} : {0}")
    @ValueSource(strings = {"test"})
    @WithCustomMockUser
    void checkNickname(String nickname) throws Exception {
        //given
        mockMvc.perform(get("/api/v1/profile/doublecheck")
                .header("Authorization", String.format("%s %s", BEARER, AUTHORIZATION))
                .param("nickname", nickname))
            .andExpect(status().isOk())
            .andDo(print());
    }

    @DisplayName("닉네임이 20자 이하이거나 비어 있는지 확인한다")
    @ParameterizedTest
    @ValueSource(strings = {"thisnicknameisexceed20characterright", " ", "\t", "\n"})
    @WithCustomMockUser
    void isExceed20Character(String nickname) throws Exception {
        //given
        mockMvc.perform(get("/api/v1/profile/doublecheck")
                .header("Authorization", String.format("%s %s", BEARER, AUTHORIZATION))
                .param("nickname", nickname))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.isValid").value(false))
            .andDo(print());

    }

    @Test
    @DisplayName("프로필 조회")
    @WithCustomMockUser
    void getProfile() throws Exception {
        //given
        ProfileInfoResponseDto mockProfile = fixtureMonkey.giveMeOne(ProfileInfoResponseDto.class);
        when(profileService.getProfileInfo(any())).thenReturn(mockProfile);
        ResultActions result = mockMvc.perform(get("/api/v1/profile")
            .header("Authorization", String.format("%s %s", BEARER, AUTHORIZATION)));

        //then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.result.profileImg").value(mockProfile.profileImg()))
            .andExpect(jsonPath("$.result.nickname").value(mockProfile.nickname()))
            .andExpect(jsonPath("$.result.birthDate").value(mockProfile.birthDate()))
            .andExpect(jsonPath("$.result.phoneNumber").value(mockProfile.phoneNumber()));
    }

}

