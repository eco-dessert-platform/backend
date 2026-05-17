package com.bbangle.bbangle.store.admin.controller;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture.NEW_STORE_NAME;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest.UpdateStoreNameRejectRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.StoreSearchResult;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.StoreSearchResult.StoreSummary;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameApprove;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameReject;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.admin.service.AdminStoreService;
import com.bbangle.bbangle.store.admin.service.model.UpdateStoreNamesInfo.UpdateStoreNames;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.domain.model.StoreNameRejectCategory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("searchStores() 테스트")
    class SearchStoresTest {

        @Test
        @DisplayName("스토어명으로 검색하면 200과 함께 결과 목록을 반환한다")
        @WithMockUser(roles = "ADMIN")
        void success_searchStores() throws Exception {
            // given
            StoreSearchResult response = StoreSearchResult.builder()
                .storeSummaries(List.of(
                    StoreSummary.builder().id(1L).name("빵그리의오븐").build(),
                    StoreSummary.builder().id(2L).name("빵그리베이커리").build()
                ))
                .totalElements(2)
                .totalPages(1)
                .hasPrevious(false)
                .hasNext(false)
                .build();
            given(adminStoreService.searchStoresByName("빵그리", 1)).willReturn(response);

            // when & then
            mockMvc.perform(get(AdminApiPath.PREFIX + "/stores/search")
                    .param("storeName", "빵그리")
                    .param("page", "1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.storeSummaries", hasSize(2)))
                .andExpect(jsonPath("$.result.storeSummaries[0].id").value(1L))
                .andExpect(jsonPath("$.result.storeSummaries[0].name").value("빵그리의오븐"))
                .andExpect(jsonPath("$.result.storeSummaries[1].id").value(2L))
                .andExpect(jsonPath("$.result.totalElements").value(2))
                .andExpect(jsonPath("$.result.totalPages").value(1))
                .andExpect(jsonPath("$.result.hasPrevious").value(false))
                .andExpect(jsonPath("$.result.hasNext").value(false));
        }

        @Test
        @DisplayName("storeName 미지정 시 전체 조회 결과를 반환한다")
        @WithMockUser(roles = "ADMIN")
        void success_searchStores_noStoreName() throws Exception {
            // given
            StoreSearchResult response = StoreSearchResult.builder()
                .storeSummaries(List.of(StoreSummary.builder().id(1L).name("빵그리의오븐").build()))
                .totalElements(1)
                .totalPages(1)
                .hasPrevious(false)
                .hasNext(false)
                .build();
            given(adminStoreService.searchStoresByName(null, 1)).willReturn(response);

            // when & then
            mockMvc.perform(get(AdminApiPath.PREFIX + "/stores/search")
                    .param("page", "1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.storeSummaries", hasSize(1)));
        }

        @Test
        @DisplayName("page=0이면 @Min(1) 검증에 의해 400을 반환한다")
        @WithMockUser(roles = "ADMIN")
        void fail_searchStores_invalidPage() throws Exception {
            mockMvc.perform(get(AdminApiPath.PREFIX + "/stores/search")
                    .param("storeName", "빵그리")
                    .param("page", "0")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }
    }

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

    @Nested
    @DisplayName("approveStoreName() 테스트")
    class ApproveStoreNameTest {

        @Test
        @DisplayName("스토어명 변경 요청을 승인한다.")
        @WithMockUser(roles = "ADMIN")
        void success_approveStoreName() throws Exception {

            // given
            long requestId = 1L;
            LocalDateTime now = LocalDateTime.now();
            String expectedTimePrefix = now.truncatedTo(ChronoUnit.SECONDS).toString();

            UpdateStoreNameApprove response = UpdateStoreNameApprove.builder()
                .storeId(1L)
                .prevName(DEFAULT_STORE_NAME)
                .updateName(NEW_STORE_NAME)
                .status(StoreApprovalStatus.APPROVE)
                .modifiedAt(now)
                .build();

            given(adminStoreService.approveStoreName(requestId)).willReturn(response);

            // when & then
            mockMvc.perform(patch(AdminApiPath.PREFIX + "/stores" + "/{requestId}/approve", requestId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.storeId").value(1L))
                .andExpect(jsonPath("$.result.prevName").value(DEFAULT_STORE_NAME))
                .andExpect(jsonPath("$.result.updateName").value(NEW_STORE_NAME))
                .andExpect(jsonPath("$.result.status").value(StoreApprovalStatus.APPROVE.name()))
                .andExpect(jsonPath("$.result.modifiedAt").value(startsWith(expectedTimePrefix)));
        }

        @Test
        @DisplayName("스토어명 변경 요청 승인 시, 이미 존재하는 이름이면 예외가 발생한다.")
        @WithMockUser(roles = "ADMIN")
        void fail_approveStoreName() throws Exception {

            // given
            long requestId = 1L;

            given(adminStoreService.approveStoreName(requestId)).willThrow(new BbangleException(BbangleErrorCode.ALREADY_RESERVED_STORE));

            // when & then
            mockMvc.perform(patch(AdminApiPath.PREFIX + "/stores/{requestId}/approve", requestId)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BbangleErrorCode.ALREADY_RESERVED_STORE.getCode()))
                .andExpect(jsonPath("$.message").value(BbangleErrorCode.ALREADY_RESERVED_STORE.getMessage()));
        }
    }

    @Nested
    @DisplayName("rejectStoreName() 테스트")
    class RejectStoreNameTest {

        @Test
        @DisplayName("스토어명 변경 요청을 거절한다.")
        @WithMockUser(roles = "ADMIN")
        void success_rejectStoreName() throws Exception {

            // given
            long requestId = 1L;
            UpdateStoreNameRejectRequest request =
                new UpdateStoreNameRejectRequest(StoreNameRejectCategory.ETC, StoreNameRejectCategory.ETC.getDescription());
            UpdateStoreNameReject response = UpdateStoreNameReject.builder()
                .requestId(1L)
                .storeId(1L)
                .currentName(DEFAULT_STORE_NAME)
                .newName(NEW_STORE_NAME)
                .status(StoreApprovalStatus.REJECT)
                .category(StoreNameRejectCategory.ETC)
                .rejectDetail(StoreNameRejectCategory.ETC.getDescription())
                .build();

            given(adminStoreService.rejectStoreName(
                eq(requestId),
                any(UpdateStoreNameRejectRequest.class)
            )).willReturn(response);

            // when & then
            mockMvc.perform(patch(AdminApiPath.PREFIX + "/stores" + "/{requestId}/reject", requestId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.requestId").value(1L))
                .andExpect(jsonPath("$.result.storeId").value(1L))
                .andExpect(jsonPath("$.result.currentName").value(DEFAULT_STORE_NAME))
                .andExpect(jsonPath("$.result.newName").value(NEW_STORE_NAME))
                .andExpect(jsonPath("$.result.status").value(StoreApprovalStatus.REJECT.name()))
                .andExpect(jsonPath("$.result.category").value(StoreNameRejectCategory.ETC.name()))
                .andExpect(jsonPath("$.result.rejectDetail").value(StoreNameRejectCategory.ETC.getDescription()));
        }

        @Test
        @DisplayName("스토어명 변경 요청 거절 시, 요청이 존재하지 않으면 예외가 발생한다.")
        @WithMockUser(roles = "ADMIN")
        void fail_rejectStoreName() throws Exception {

            // given
            long requestId = 1L;

            UpdateStoreNameRejectRequest request =
                new UpdateStoreNameRejectRequest(
                    StoreNameRejectCategory.ETC,
                    StoreNameRejectCategory.ETC.getDescription()
                );

            given(adminStoreService.rejectStoreName(
                eq(requestId),
                any(UpdateStoreNameRejectRequest.class)
            )).willThrow(new BbangleException(BbangleErrorCode.NOT_FOUND_REQUEST));

            // when & then
            mockMvc.perform(patch(AdminApiPath.PREFIX + "/stores/{requestId}/reject", requestId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BbangleErrorCode.NOT_FOUND_REQUEST.getCode()))
                .andExpect(jsonPath("$.message").value(BbangleErrorCode.NOT_FOUND_REQUEST.getMessage()));
        }
    }
}