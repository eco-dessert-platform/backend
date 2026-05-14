package com.bbangle.bbangle.store.admin.controller;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_PROFILE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_DETAIL_ADDRESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_EMAIL;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_IDENTIFIER;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_INTRODUCE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_SUBPHONE;
import static com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture.NEW_STORE_NAME;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import com.bbangle.bbangle.fixture.store.admin.controller.dto.StoreDetailRequestFixture;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest.UpdateStoreNameRejectRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.StoreDetailResponse;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameApprove;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameReject;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.admin.facade.AdminStoreFacade;
import com.bbangle.bbangle.store.admin.service.AdminStoreService;
import com.bbangle.bbangle.store.admin.service.model.UpdateStoreNamesInfo.UpdateStoreNames;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.domain.model.StoreNameRejectCategory;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

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

    @MockBean
    private AdminStoreFacade adminStoreFacade;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Nested
    @DisplayName("createStoreForAdmin() 테스트")
    class CreateStoreForAdminTest {

        private MockMultipartFile createImageFile(
            AdminStoreRequest.StoreDetailRequest request
        ) throws JsonProcessingException {
            return new MockMultipartFile(
                "request",
                "request.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
            );
        }

        private MockMultipartFile createProfileFile() {
            return new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "profile image content".getBytes()
            );
        }

        @Test
        @DisplayName("관리자 스토어 생성에 성공한다")
        @WithMockUser(roles = "ADMIN")
        void success_createStoreForAdmin() throws Exception {

            // given
            AdminStoreRequest.StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();
            MockMultipartFile requestPart = createImageFile(request);
            MockMultipartFile profileImage = createProfileFile();
            StoreDetailResponse response = StoreDetailResponse.builder()
                .storeId(1L)
                .name(request.storeName())
                .identifier(request.identifier())
                .introduce(request.introduce())
                .profile("https://cdn.test/profile.png")
                .phoneNumber(request.phoneNumber())
                .subPhoneNumber(request.subPhoneNumber())
                .email(request.email())
                .originAddress(request.originAddress())
                .originAddressDetail(request.originAddressDetail())
                .build();

            given(adminStoreFacade.createStoreForAdmin(
                any(AdminStoreRequest.StoreDetailRequest.class),
                any(MultipartFile.class))
            ).willReturn(response);

            // when & then
            mockMvc.perform(
                    multipart(AdminApiPath.PREFIX + "/stores")
                        .file(requestPart)
                        .file(profileImage)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.storeId").value(1L))
                .andExpect(jsonPath("$.result.name").value(request.storeName()))
                .andExpect(jsonPath("$.result.identifier").value(request.identifier()))
                .andExpect(jsonPath("$.result.introduce").value(request.introduce()));
        }

        @Test
        @DisplayName("요청값이 유효하지 않으면 400 반환")
        @WithMockUser(roles = "ADMIN")
        void fail_createStoreForAdmin_validation() throws Exception {

            // given
            AdminStoreRequest.StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture("");

            MockMultipartFile requestPart = createImageFile(request);

            // when & then
            mockMvc.perform(
                multipart(AdminApiPath.PREFIX + "/stores")
                    .file(requestPart)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("스토어 이름 중복 시 예외 반환")
        @WithMockUser(roles = "ADMIN")
        void fail_createStoreForAdmin_duplicateStoreName() throws Exception {

            // given
            AdminStoreRequest.StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();
            MockMultipartFile requestPart = createImageFile(request);
            MockMultipartFile profileImage = createProfileFile();

            given(adminStoreFacade.createStoreForAdmin(any(), any())).willThrow(new BbangleException(BbangleErrorCode.INVALID_STORE_NAME));

            // when & then
            mockMvc.perform(
                multipart(AdminApiPath.PREFIX + "/stores")
                    .file(requestPart)
                    .file(profileImage)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("updateStoreForAdmin() 테스트")
    class UpdateStoreForAdminTest {

        @Test
        @DisplayName("스토어 상세 정보 수정에 성공한다")
        @WithMockUser(roles = "ADMIN")
        void success_update_store() throws Exception {

            // given
            Long storeId = 1L;
            AdminStoreRequest.StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();
            StoreDetailResponse response = StoreDetailResponse.builder()
                .storeId(storeId)
                .name(request.storeName())
                .identifier(request.identifier())
                .introduce(request.introduce())
                .profile(DEFAULT_PROFILE)
                .phoneNumber(request.phoneNumber())
                .subPhoneNumber(request.subPhoneNumber())
                .email(request.email())
                .originAddress(request.originAddress())
                .originAddressDetail(request.originAddressDetail())
                .build();

            given(adminStoreService.updateStoreWithName(
                eq(storeId),
                any(AdminStoreRequest.StoreDetailRequest.class)
            )).willReturn(response);

            // when & then
            mockMvc.perform(
                    patch(AdminApiPath.PREFIX + "/stores/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value(NEW_STORE_NAME))
                .andExpect(jsonPath("$.result.identifier").value(NEW_IDENTIFIER))
                .andExpect(jsonPath("$.result.profile").value(DEFAULT_PROFILE));

            verify(adminStoreService).updateStoreWithName(eq(storeId), any(AdminStoreRequest.StoreDetailRequest.class));
        }

        @Test
        @DisplayName("스토어 이름이 없으면 검증에 실패한다")
        @WithMockUser(roles = "ADMIN")
        void fail_blank_store_name() throws Exception {

            // given
            Long storeId = 1L;
            AdminStoreRequest.StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture("");

            // when & then
            mockMvc.perform(
                    patch(AdminApiPath.PREFIX + "/stores/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

            verify(adminStoreService, never()).updateStoreWithName(anyLong(), any());
            verify(responseService, never()).getSingleResult(any());
        }

        @Test
        @DisplayName("잘못된 전화번호 형식이면 검증에 실패한다")
        @WithMockUser(roles = "ADMIN")
        void fail_invalid_phone() throws Exception {

            // given
            Long storeId = 1L;
            AdminStoreRequest.StoreDetailRequest request = new AdminStoreRequest.StoreDetailRequest(
                NEW_STORE_NAME,
                NEW_IDENTIFIER,
                NEW_INTRODUCE,
                "invalid-phone",
                NEW_SUBPHONE,
                NEW_EMAIL,
                NEW_ADDRESS,
                NEW_DETAIL_ADDRESS
            );

            // when & then
            mockMvc.perform(
                    patch(AdminApiPath.PREFIX + "/stores/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

            verify(adminStoreService, never()).updateStoreWithName(anyLong(), any());
        }

        @Test
        @DisplayName("스토어가 존재하지 않으면 예외를 반환한다")
        @WithMockUser(roles = "ADMIN")
        void fail_store_not_found() throws Exception {

            // given
            Long storeId = 999L;
            AdminStoreRequest.StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();

            given(adminStoreService.updateStoreWithName(
                eq(storeId), any(AdminStoreRequest.StoreDetailRequest.class))
            ).willThrow(new BbangleException(BbangleErrorCode.STORE_NOT_FOUND));

            // when & then
            mockMvc.perform(
                    patch(AdminApiPath.PREFIX + "/stores/{storeId}", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
        }
    }
}