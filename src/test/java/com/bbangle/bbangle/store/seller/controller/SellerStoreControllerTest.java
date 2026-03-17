package com.bbangle.bbangle.store.seller.controller;

import static com.bbangle.bbangle.common.service.ResponseService.CommonResponse.SUCCESS;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture.NEW_STORE_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.client.annotation.WithMockAuthenticationPrincipal;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.store.seller.controller.dto.SellerStoreRequestFixture;
import com.bbangle.bbangle.fixture.store.seller.controller.dto.SellerStoreResponseFixture;
import com.bbangle.bbangle.fixture.store.seller.controller.dto.StoreApplicationRequestFixture;
import com.bbangle.bbangle.fixture.store.seller.controller.dto.StoreApplicationResponseFixture;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationRequest.StoreApplicationCreateRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationResponse.StoreApplicationDetail;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreDetail;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreNameCheck;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.UpdateStoreNameResponse;
import com.bbangle.bbangle.store.seller.facade.SellerStoreApplicationFacade;
import com.bbangle.bbangle.store.seller.facade.SellerStoreFacade;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

@DisplayName("[컨트롤러 테스트] SellerStoreController")
@WebMvcTest(controllers = SellerStoreController.class)
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@ActiveProfiles("test")
class SellerStoreControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonDataEncoder jsonDataEncoder;

    @MockBean
    private SellerStoreService sellerStoreService;

    @MockBean
    private SellerStoreFacade sellerStoreFacade;

    @MockBean
    private SellerStoreApplicationFacade sellerStoreApplicationFacade;

    @SpyBean
    private ResponseService responseService;

    @Nested
    @DisplayName("registerStore() 테스트")
    class RegisterStoreTest {

        private MockMultipartFile createImageFile(
            StoreApplicationCreateRequest request
        ) throws JsonProcessingException  {
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
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("스토어 등록 신청하면 스토어 등록 신청 데이터를 반환한다.")
        void success_registerStore() throws Exception {

            // given
            StoreApplicationCreateRequest request = StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest();
            MockMultipartFile requestPart = createImageFile(request);
            MockMultipartFile profileImage = createProfileFile();
            StoreApplicationDetail response = StoreApplicationResponseFixture.defaultStoreApplicationDetail(null);

            given(sellerStoreApplicationFacade.registerStoreForSeller(
                eq(1L),
                any(StoreApplicationCreateRequest.class),
                any(MultipartFile.class)
            )).willReturn(response);

            // when & then
            mockMvc.perform(
                    multipart(SellerApiPath.PREFIX + "/stores/applications")
                        .file(requestPart)
                        .file(profileImage)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sellerId").value(1L))
                .andExpect(jsonPath("$.result.storeApplicationId").value(1L))
                .andExpect(jsonPath("$.result.name").value(DEFAULT_STORE_NAME));
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("스토어명이 비어있는 경우 스토어 등록 신청에 실패한다.")
        void fail_RegisterStoreTest_invalid_request() throws Exception {

            // given
            StoreApplicationCreateRequest request = StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest("");
            MockMultipartFile requestPart = createImageFile(request);
            MockMultipartFile profileImage = createProfileFile();

            // when & then
            mockMvc.perform(
                    multipart(SellerApiPath.PREFIX + "/stores/applications")
                        .file(requestPart)
                        .file(profileImage)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("이미 스토어를 등록했거나 승인 대기 중인 판매자인 경우 스토어 등록 신청에 실패한다.")
        void fail_RegisterStoreTest_already_registered() throws Exception {

            // given
            StoreApplicationCreateRequest request = StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest();
            MockMultipartFile requestPart = createImageFile(request);
            MockMultipartFile profileImage = createProfileFile();

            given(sellerStoreApplicationFacade.registerStoreForSeller(anyLong(), any(), any()))
                .willThrow(new BbangleException(BbangleErrorCode.ALREADY_REGISTER_STORE));

            // when & then
            mockMvc.perform(
                    multipart(SellerApiPath.PREFIX + "/stores/applications")
                        .file(requestPart)
                        .file(profileImage)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("스토어 이름이 중복될 경우 스토어 등록 신청에 실패한다.")
        void fail_RegisterStoreTest_duplicate_storeName() throws Exception {

            // given
            StoreApplicationCreateRequest request = StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest();
            MockMultipartFile requestPart = createImageFile(request);
            MockMultipartFile profileImage = createProfileFile();

            given(sellerStoreApplicationFacade.registerStoreForSeller(anyLong(), any(), any()))
                .willThrow(new BbangleException(BbangleErrorCode.INVALID_STORE_NAME));

            // when & then
            mockMvc.perform(
                    multipart(SellerApiPath.PREFIX + "/stores/applications")
                        .file(requestPart)
                        .file(profileImage)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("이미 등록된 스토어일 경우 스토어 등록 신청에 실패한다.")
        void fail_RegisterStoreTest_already_reserved() throws Exception {

            // given
            StoreApplicationCreateRequest request = StoreApplicationRequestFixture.defaultStoreApplicationCreateRequest(1L);
            MockMultipartFile requestPart = createImageFile(request);
            MockMultipartFile profileImage = createProfileFile();

            given(sellerStoreApplicationFacade.registerStoreForSeller(anyLong(), any(), any()))
                .willThrow(new BbangleException(BbangleErrorCode.ALREADY_RESERVED_STORE));

            // when & then
            mockMvc.perform(
                    multipart(SellerApiPath.PREFIX + "/stores/applications")
                        .file(requestPart)
                        .file(profileImage)
                )
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("getStoreApplication() 테스트")
    class GetStoreApplicationTest {

        @Test
        @DisplayName("스토어 등록 신청 내역이 없을 경우 null을 반환한다.")
        @WithMockAuthenticationPrincipal(role = "SELLER")
        void notExist_storeApplication() throws Exception {

            // given
            given(sellerStoreApplicationFacade.findStoreApplication(anyLong())).willReturn(null);

            // when & then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isEmpty());
        }

        @Test
        @DisplayName("스토어 등록 신청 내역이 존재할 경우 Response를 반환한다.")
        @WithMockAuthenticationPrincipal(role = "SELLER")
        void exist_storeApplication() throws Exception {

            // given
            StoreApplicationDetail response = StoreApplicationResponseFixture.defaultStoreApplicationDetail(null);
            given(sellerStoreApplicationFacade.findStoreApplication(anyLong())).willReturn(response);

            // when & then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sellerId").value(1L))
                .andExpect(jsonPath("$.result.storeApplicationId").value(1L))
                .andExpect(jsonPath("$.result.name").value(DEFAULT_STORE_NAME));
        }
    }

    @Nested
    @DisplayName("search() 테스트")
    class SearchTest {

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("스토어 이름으로 스토어 목록을 반환한다.")
        void success_search() throws Exception {

            // given
            String storeName = "빵";
            StoreInfo storeInfo = StoreInfo.builder()
                .id(1L)
                .name("빵굽는하루")
                .build();

            CursorPagination<StoreInfo> pagination = CursorPagination.of(List.of(storeInfo),20, null, StoreInfo::id);
            given(sellerStoreService.selectStoreNameForSeller(storeName, null)).willReturn(pagination);

            // when & then
            mockMvc.perform(get(SellerApiPath.PREFIX  + "/stores/store-names")
                    .param("storeName", storeName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
                .andExpect(jsonPath("$.result.content[0].id").value(1L))
                .andExpect(jsonPath("$.result.content[0].name").value("빵굽는하루"))
                .andExpect(jsonPath("$.result.nextCursor").value(-1L))
                .andExpect(jsonPath("$.result.hasNext").value(false));
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("스토어 이름과 cursorId로 스토어 목록을 반환한다.")
        void success_search_cursorId() throws Exception {

            // given
            String storeName = "빵";
            Long cursorId = 21L;
            StoreInfo storeInfo = StoreInfo.builder()
                .id(21L)
                .name("빵굽는하루")
                .build();

            CursorPagination<SellerStoreInfo.StoreInfo> pagination =
                new CursorPagination<>(List.of(storeInfo), -1L, false, null);
            given(sellerStoreService.selectStoreNameForSeller(storeName, cursorId)).willReturn(pagination);

            // when & then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores/store-names")
                    .param("storeName", storeName)
                    .param("cursorId", "21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
                .andExpect(jsonPath("$.result.content[0].id").value(21L))
                .andExpect(jsonPath("$.result.content[0].name").value("빵굽는하루"))
                .andExpect(jsonPath("$.result.nextCursor").value(-1L))
                .andExpect(jsonPath("$.result.hasNext").value(false));
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("storeName이 공백일 경우 400에러를 반환한다.")
        void failure_search_400() throws Exception {

            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores/store-names")
                    .param("storeName", "  "))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("checkStoreNameDuplicate() 테스트")
    class CheckStoreNameDuplicateTest {

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("조회한 스토어가 존재하지 않을 경우 사용 가능하다.")
        void success_checkStoreNameDuplicate_notExist_available() throws Exception {

            // given
            String storeName = "빵";
            StoreResponse.StoreNameCheck response = StoreNameCheck.builder()
                .available(true)
                .store(null)
                .build();

            // when
            given(sellerStoreFacade.checkStoreName(storeName)).willReturn(response);

            // then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores/check-name")
                    .param("storeName", storeName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
                .andExpect(jsonPath("$.result.available").value(true))
                .andExpect(jsonPath("$.result.store").isEmpty());
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("조회한 스토어를 등록한 판매자가 없을 경우 사용 가능하다.")
        void success_checkStoreNameDuplicate_statusNone_available() throws Exception {

            // given
            String storeName = "빵";
            StoreResponse.SellerStoreDetail sellerStoreDetail =
                new SellerStoreDetail(1L, "빵긋", "테스트", "test.png", "01012345678", "01098765432", "123@test.com", "서울", "123동");
            StoreResponse.StoreNameCheck response = StoreNameCheck.builder()
                .available(true)
                .store(sellerStoreDetail)
                .build();

            // when
            given(sellerStoreFacade.checkStoreName(storeName)).willReturn(response);

            // then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores/check-name")
                    .param("storeName", storeName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
                .andExpect(jsonPath("$.result.available").value(true))
                .andExpect(jsonPath("$.result.store.storeId").value(1L))
                .andExpect(jsonPath("$.result.store.name").value("빵긋"))
                .andExpect(jsonPath("$.result.store.introduce").value("테스트"))
                .andExpect(jsonPath("$.result.store.profile").value("test.png"));
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("조회한 스토어의 상태가 None이 아닐 경우 사용할 수 없다.")
        void success_checkStoreNameDuplicate_statusNotNone_unavailable() throws Exception {

            // given
            String storeName = "빵";
            StoreResponse.SellerStoreDetail sellerStoreDetail =
                new SellerStoreDetail(1L, "빵긋", "테스트", "test.png", "01012345678", "01098765432", "123@test.com", "서울", "123동");
            StoreResponse.StoreNameCheck response = StoreNameCheck.builder()
                .available(false)
                .store(sellerStoreDetail)
                .build();

            // when
            given(sellerStoreFacade.checkStoreName(storeName)).willReturn(response);

            // then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores" + "/check-name")
                    .param("storeName", storeName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
                .andExpect(jsonPath("$.result.available").value(false))
                .andExpect(jsonPath("$.result.store.storeId").value(1L))
                .andExpect(jsonPath("$.result.store.name").value("빵긋"))
                .andExpect(jsonPath("$.result.store.introduce").value("테스트"))
                .andExpect(jsonPath("$.result.store.profile").value("test.png"));
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("storeName이 공백일 경우 400에러를 반환한다.")
        void failure_checkStoreNameDuplicate_400() throws Exception {

            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores" + "/check-name")
                    .param("storeName", "  "))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("getRegisteredStoreDetail() 테스트")
    class GetRegisteredStoreDetailTest {

        @Test
        @DisplayName("등록된 스토어가 존재할 경우 해당 스토어 정보를 조회한다.")
        @WithMockAuthenticationPrincipal(role = "SELLER")
        void getRegisteredStoreDetail_exist_registeredStore() throws Exception {

            // given
            Long sellerId = 1L;
            StoreResponse.SellerStoreDetail sellerStoreDetail =
                new SellerStoreDetail(1L, "빵긋", "테스트", "test.png", "01012345678", "01098765432", "123@test.com", "서울", "123동");

            StoreResponse.StoreNameCheck response = StoreResponse.StoreNameCheck.builder()
                .available(true)
                .store(sellerStoreDetail)
                .build();

            given(sellerStoreFacade.getRegisteredStoreDetail(sellerId)).willReturn(response);

            // when & then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.available").value(true))
                .andExpect(jsonPath("$.result.store.storeId").value(sellerStoreDetail.storeId()))
                .andExpect(jsonPath("$.result.store.name").value(sellerStoreDetail.name()))
                .andExpect(jsonPath("$.result.store.introduce").value(sellerStoreDetail.introduce()))
                .andExpect(jsonPath("$.result.store.profile").value(sellerStoreDetail.profile()))
                .andExpect(jsonPath("$.result.store.phoneNumber").value(sellerStoreDetail.phoneNumber()))
                .andExpect(jsonPath("$.result.store.subPhoneNumber").value(sellerStoreDetail.subPhoneNumber()))
                .andExpect(jsonPath("$.result.store.email").value(sellerStoreDetail.email()))
                .andExpect(jsonPath("$.result.store.originAddress").value(sellerStoreDetail.originAddress()))
                .andExpect(jsonPath("$.result.store.originAddressDetail").value(sellerStoreDetail.originAddressDetail()));

            verify(sellerStoreFacade).getRegisteredStoreDetail(sellerId);
        }

        @Test
        @DisplayName("스토어명을 변경한 이력이 있을 경우 스토어명 변경 신청을 할 수 없다.")
        @WithMockAuthenticationPrincipal(role = "SELLER")
        void getRegisteredStoreDetail_updateStoreName_approved() throws Exception {

            // given
            Long sellerId = 1L;
            StoreResponse.SellerStoreDetail sellerStoreDetail =
                new SellerStoreDetail(1L, "빵긋", "테스트", "test.png", "01012345678", "01098765432", "123@test.com", "서울", "123동");

            StoreResponse.StoreNameCheck response = StoreResponse.StoreNameCheck.builder()
                .available(false)
                .store(sellerStoreDetail)
                .build();

            given(sellerStoreFacade.getRegisteredStoreDetail(sellerId)).willReturn(response);

            // when & then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.available").value(false))
                .andExpect(jsonPath("$.result.store.storeId").value(sellerStoreDetail.storeId()))
                .andExpect(jsonPath("$.result.store.name").value(sellerStoreDetail.name()))
                .andExpect(jsonPath("$.result.store.introduce").value(sellerStoreDetail.introduce()))
                .andExpect(jsonPath("$.result.store.profile").value(sellerStoreDetail.profile()))
                .andExpect(jsonPath("$.result.store.phoneNumber").value(sellerStoreDetail.phoneNumber()))
                .andExpect(jsonPath("$.result.store.subPhoneNumber").value(sellerStoreDetail.subPhoneNumber()))
                .andExpect(jsonPath("$.result.store.email").value(sellerStoreDetail.email()))
                .andExpect(jsonPath("$.result.store.originAddress").value(sellerStoreDetail.originAddress()))
                .andExpect(jsonPath("$.result.store.originAddressDetail").value(sellerStoreDetail.originAddressDetail()));

            verify(sellerStoreFacade).getRegisteredStoreDetail(sellerId);
        }

        @Test
        @DisplayName("등록된 스토어가 없을 경우 404 에러를 반환한다.")
        @WithMockAuthenticationPrincipal(role = "SELLER")
        void getRegisteredStoreDetail_noExist_registeredStore() throws Exception {

            // given
            given(sellerStoreFacade.getRegisteredStoreDetail(1L))
                .willThrow(new BbangleException(BbangleErrorCode.NOT_REGISTERED_STORE));

            // when & then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores"))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("updateStoreName() 테스트")
    class UpdateStoreNameTest {

        @Test
        @DisplayName("스토어명 변경 신청에 성공한다.")
        @WithMockAuthenticationPrincipal(role = "SELLER")
        void success_updateStoreName() throws Exception {

            // given
            Long sellerId = 1L;
            UpdateStoreNameRequest request = SellerStoreRequestFixture.defaultUpdateStoreNameRequest();
            UpdateStoreNameResponse response = SellerStoreResponseFixture.defaultUpdateStoreNameResponse();

            given(sellerStoreFacade.updateStoreName(eq(sellerId), any(UpdateStoreNameRequest.class))).willReturn(response);

            // when & then
            mockMvc.perform(post(SellerApiPath.PREFIX + "/stores/store-names")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonDataEncoder.encode(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
                .andExpect(jsonPath("$.result.sellerId").value(1L))
                .andExpect(jsonPath("$.result.storeId").value(1L))
                .andExpect(jsonPath("$.result.storeNameRequestId").value(1L))
                .andExpect(jsonPath("$.result.currentName").value(DEFAULT_STORE_NAME))
                .andExpect(jsonPath("$.result.newName").value(NEW_STORE_NAME))
                .andExpect(jsonPath("$.result.status").value(StoreApprovalStatus.PENDING.name()))
                .andExpect(jsonPath("$.result.rejectCategory").isEmpty())
                .andExpect(jsonPath("$.result.rejectDetail").isEmpty());
        }

        @Test
        @DisplayName("스토어명 중복 시 스토어명 변경 신청에 실패한다.")
        @WithMockAuthenticationPrincipal(role = "SELLER")
        void fail_updateStoreName() throws Exception {

            // given
            given(sellerStoreFacade.updateStoreName(anyLong(), any()))
                .willThrow(new BbangleException(BbangleErrorCode.ALREADY_RESERVED_STORE));

            // when & then
            mockMvc.perform(post(SellerApiPath.PREFIX + "/stores/store-names")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonDataEncoder.encode(
                        SellerStoreRequestFixture.defaultUpdateStoreNameRequest()
                    ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BbangleErrorCode.ALREADY_RESERVED_STORE.getCode()))
                .andExpect(jsonPath("$.message").value(BbangleErrorCode.ALREADY_RESERVED_STORE.getMessage()));
        }
    }
}