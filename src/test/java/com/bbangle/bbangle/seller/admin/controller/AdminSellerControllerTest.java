package com.bbangle.bbangle.seller.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.AdminApiPath;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerRequest;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationList;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationRejectList;
import com.bbangle.bbangle.seller.admin.facade.AdminSellerFacade;
import com.bbangle.bbangle.seller.admin.service.AdminSellerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminSellerFacade adminSellerFacade;

    @MockBean
    private AdminSellerService adminSellerService;

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

    @Nested
    @DisplayName("rejectSellerApplications() 테스트")
    class RejectSellerApplicationsTest {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("판매자의 스토어 등록 신청을 거절한다.")
        void success_rejectSellerApplications() throws Exception {

            // given
            List<Long> ids = List.of(1L, 2L);

            AdminSellerRequest.StoreApplicationIds request = new AdminSellerRequest.StoreApplicationIds(ids);

            AdminSellerApplicationRejectList serviceResult = AdminSellerApplicationRejectList.builder()
                .successIds(ids)
                .failDetails(List.of())
                .build();

            given(adminSellerService.rejectStoreApplications(ids)).willReturn(serviceResult);

            // when & then
            mockMvc.perform(patch(AdminApiPath.PREFIX + "/sellers/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.successIds").isArray())
                .andExpect(jsonPath("$.result.successIds[0]").value(1L))
                .andExpect(jsonPath("$.result.successIds[1]").value(2L))
                .andExpect(jsonPath("$.result.failDetails").isEmpty());

            verify(adminSellerService).rejectStoreApplications(ids);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Request가 없는 경우 예외를 반환한다.")
        void fail_rejectSellerApplications_validation() throws Exception {

            // given
            String invalidJson = "{}";

            // when & then
            mockMvc.perform(patch(AdminApiPath.PREFIX + "/sellers/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson)
                )
                .andExpect(status().isBadRequest());

            verify(adminSellerService, never()).rejectStoreApplications(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Request가 비어있는 리스트일 경우 예외를 반환한다.")
        void rejectSellerApplications_empty_ids() throws Exception {

            // given
            AdminSellerRequest.StoreApplicationIds request = new AdminSellerRequest.StoreApplicationIds(List.of());

            // when & then
            mockMvc.perform(patch(AdminApiPath.PREFIX + "/sellers/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

            verify(adminSellerService, never()).rejectStoreApplications(any());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("ADMIN 권한이 없으면 접근이 거부된다.")
        void rejectSellerApplications_forbidden_for_user_role() throws Exception {

            // given
            AdminSellerRequest.StoreApplicationIds request = new AdminSellerRequest.StoreApplicationIds(List.of(1L));

            // when & then
            mockMvc.perform(patch(AdminApiPath.PREFIX + "/sellers/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

            verify(adminSellerService, never()).rejectStoreApplications(any());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("일부 실패가 있을 때 응답에 failDetails가 포함된다.")
        void rejectSellerApplications_with_partial_fail_details() throws Exception {

            // given
            List<Long> ids = List.of(1L, 999L);
            AdminSellerRequest.StoreApplicationIds request = new AdminSellerRequest.StoreApplicationIds(ids);

            AdminSellerApplicationRejectList.FailDetail failDetail =
                AdminSellerApplicationRejectList.FailDetail.builder()
                    .storeApplicationId(999L)
                    .reason("해당 요청을 찾을 수 없습니다.")
                    .build();

            AdminSellerApplicationRejectList serviceResult = AdminSellerApplicationRejectList.builder()
                .successIds(List.of(1L))
                .failDetails(List.of(failDetail))
                .build();

            given(adminSellerService.rejectStoreApplications(ids)).willReturn(serviceResult);

            // when & then
            mockMvc.perform(patch(AdminApiPath.PREFIX + "/sellers/reject")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.successIds[0]").value(1L))
                .andExpect(jsonPath("$.result.failDetails").isArray())
                .andExpect(jsonPath("$.result.failDetails[0].storeApplicationId").value(999L))
                .andExpect(jsonPath("$.result.failDetails[0].reason").value("해당 요청을 찾을 수 없습니다."));
        }
    }
}