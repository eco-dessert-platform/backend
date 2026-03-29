package com.bbangle.bbangle.store.admin.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.AdminApiPath;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.admin.service.AdminStoreService;
import com.bbangle.bbangle.store.admin.service.model.UpdateStoreNamesInfo.UpdateStoreNames;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("[컨트롤러 테스트] AdminStoreController")
@WebMvcTest(controllers = AdminStoreController.class)
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@ActiveProfiles("test")
class AdminStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private ResponseService responseService;

    @MockBean
    private AdminStoreService adminStoreService;

    @Test
    @DisplayName("스토어명 변경 요청 목록을 조회한다.")
    @WithMockUser(roles = "ADMIN")
    void getUpdateStoreNames_success() throws Exception {

        // given
        int page = 1;

        List<UpdateStoreNames> content = List.of(
            UpdateStoreNames.builder()
                .storeId(1L)
                .currentName("oldName1")
                .newName("newName1")
                .createdAt(LocalDateTime.of(2026, 3, 26, 10, 0))
                .build(),
            UpdateStoreNames.builder()
                .storeId(2L)
                .currentName("oldName2")
                .newName("newName2")
                .createdAt(LocalDateTime.of(2026, 3, 26, 11, 0))
                .build()
        );

        UpdateStoreNameRequest responseDto = UpdateStoreNameRequest.builder()
            .updateStoreNames(content)
            .totalElements(2)
            .totalPages(1)
            .hasPrevious(false)
            .hasNext(false)
            .build();

        given(adminStoreService.getPendingRequests(page)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get(AdminApiPath.PREFIX + "/stores")
                .param("page", String.valueOf(page))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            // 공통 응답 구조
            .andExpect(jsonPath("$.success").value(true))

            .andExpect(jsonPath("$.result.totalElements").value(2))
            .andExpect(jsonPath("$.result.totalPages").value(1))
            .andExpect(jsonPath("$.result.hasPrevious").value(false))
            .andExpect(jsonPath("$.result.hasNext").value(false))
            // 리스트 검증
            .andExpect(jsonPath("$.result.updateStoreNames", hasSize(2)))
            .andExpect(jsonPath("$.result.updateStoreNames[0].storeId").value(1L))
            .andExpect(jsonPath("$.result.updateStoreNames[0].currentName").value("oldName1"))
            .andExpect(jsonPath("$.result.updateStoreNames[0].newName").value("newName1"))
            .andExpect(jsonPath("$.result.updateStoreNames[0].createdAt").exists())

            .andExpect(jsonPath("$.result.updateStoreNames[1].storeId").value(2L));
    }
}