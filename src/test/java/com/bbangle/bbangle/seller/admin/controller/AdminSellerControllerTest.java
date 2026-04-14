package com.bbangle.bbangle.seller.admin.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationList;
import com.bbangle.bbangle.seller.admin.facade.AdminSellerFacade;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("[컨트롤러 테스트] AdminSellerController")
@WebMvcTest(controllers = AdminSellerController.class)
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@ActiveProfiles("test")
class AdminSellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminSellerFacade adminSellerFacade;

    @SpyBean
    private ResponseService responseService;

    @Nested
    @DisplayName("getSellerApplicationList() 테스트")
    class GetSellerApplicationListTest {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("판매자의 스토어 등록 신청 목록 조회에 성공한다.")
        void success_getSellerApplicationList() throws Exception {

            // given
            int page = 1;
            AdminSellerApplicationList response = AdminSellerApplicationList.builder()
                .adminSellerApplicationList(List.of())
                .totalPages(2)
                .totalElements(150)
                .hasPrevious(false)
                .hasNext(true)
                .build();

            given(adminSellerFacade.getAdminSellerApplicationList(page)).willReturn(response);

            // when & then
            mockMvc.perform(get(AdminApiPath.PREFIX + "/sellers")
                    .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalPages").value(2))
                .andExpect(jsonPath("$.result.totalElements").value(150))
                .andExpect(jsonPath("$.result.hasPrevious").value(false))
                .andExpect(jsonPath("$.result.hasNext").value(true));

            verify(adminSellerFacade).getAdminSellerApplicationList(page);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("page 파라미터가 없으면 기본값 1로 조회한다.")
        void success_getSellerApplicationList_notExist_param() throws Exception {

            // given
            int page = 1;
            AdminSellerApplicationList response = AdminSellerApplicationList.builder()
                .adminSellerApplicationList(List.of())
                .totalPages(1)
                .totalElements(0)
                .hasPrevious(false)
                .hasNext(false)
                .build();

            given(adminSellerFacade.getAdminSellerApplicationList(page)).willReturn(response);

            // when & then
            mockMvc.perform(get(AdminApiPath.PREFIX + "/sellers")).andExpect(status().isOk());
            verify(adminSellerFacade).getAdminSellerApplicationList(1);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("page가 1보다 작으면 검증 실패한다.")
        void failure_getSellerApplicationList() throws Exception {

            // when & then
            mockMvc.perform(get(AdminApiPath.PREFIX + "/sellers")
                    .param("page", "0"))
                .andExpect(status().isBadRequest());
            verify(adminSellerFacade, never()).getAdminSellerApplicationList(anyInt());
        }
    }
}