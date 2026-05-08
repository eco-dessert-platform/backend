package com.bbangle.bbangle.seller.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerRequest.StoreApplicationApprove;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationApproveList;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationApproveList.SuccessDetail;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationList;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationRejectList;
import com.bbangle.bbangle.seller.admin.facade.AdminSellerFacade;
import com.bbangle.bbangle.seller.admin.service.AdminSellerService;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
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
    }

    @Nested
    @DisplayName("approveSellerApplications() 테스트")
    class ApproveSellerApplicationsTest {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("스토어 신청 승인 요청에 성공한다")
        void approveSellerApplications_success() throws Exception {

            // given
            StoreApplicationApprove request = StoreApplicationApprove.builder()
                .applicationId(1L)
                .sellerName("홍길동")
                .identifier("12345")
                .build();

            AdminSellerApplicationApproveList response = AdminSellerApplicationApproveList.builder()
                .successDetails(
                    List.of(SuccessDetail.builder()
                        .storeApplicationId(1L)
                        .storeApplicationStatus(StoreApprovalStatus.APPROVE)
                        .build()))
                .failDetails(List.of())
                .build();

            given(adminSellerFacade.approveStoreApplications(anyList())).willReturn(response);

            // when & then
            mockMvc.perform(put(AdminApiPath.PREFIX + "/sellers/approve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(List.of(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.successDetails.length()").value(1))
                .andExpect(jsonPath("$.result.failDetails.length()").value(0))
                .andExpect(jsonPath("$.result.successDetails[0].storeApplicationId").value(1L));

            verify(adminSellerFacade).approveStoreApplications(anyList());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("스토어 신청 승인 요청 시 applicationId가 null이면 검증에 실패한다")
        void approveSellerApplications_validationFail_applicationId() throws Exception {

            // given
            String request = """
            [
              {
                "applicationId": null,
                "sellerName": "홍길동",
                "identifier": "12345"
              }
            ]
            """;

            // when & then
            mockMvc.perform(put(AdminApiPath.PREFIX + "/sellers/approve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isBadRequest());
            verify(adminSellerFacade, never()).approveStoreApplications(anyList());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("스토어 신청 승인 요청 시 sellerName이 비어있으면 검증에 실패한다")
        void approveSellerApplications_validationFail_sellerName() throws Exception {

            // given
            String request = """
            [
              {
                "applicationId": 1,
                "sellerName": "",
                "identifier": "12345"
              }
            ]
            """;

            // when & then
            mockMvc.perform(put(AdminApiPath.PREFIX + "/sellers/approve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isBadRequest());
            verify(adminSellerFacade, never()).approveStoreApplications(anyList());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("스토어 신청 승인 요청 시 identifier가 비어있으면 검증에 실패한다")
        void approveSellerApplications_validationFail_identifier() throws Exception {

            // given
            String request = """
            [
              {
                "applicationId": 1,
                "sellerName": "홍길동",
                "identifier": ""
              }
            ]
            """;

            // when & then
            mockMvc.perform(
                put(AdminApiPath.PREFIX + "/sellers/approve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isBadRequest());
            verify(adminSellerFacade, never()).approveStoreApplications(anyList());
        }
    }
}