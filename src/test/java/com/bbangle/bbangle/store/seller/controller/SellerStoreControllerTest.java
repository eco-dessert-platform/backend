package com.bbangle.bbangle.store.seller.controller;

import static com.bbangle.bbangle.common.service.ResponseService.CommonResponse.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest.StoreCreateRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreDetail;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreNameCheck;
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

    @MockBean
    private SellerStoreService sellerStoreService;

    @MockBean
    private SellerStoreFacade sellerStoreFacade;

    @SpyBean
    private ResponseService responseService;

    @Nested
    @DisplayName("registerStore() 테스트")
    class RegisterStoreTest {

        private StoreRequest.StoreCreateRequest createRequest(String storeName) {
            return new StoreCreateRequest(
                storeName,
                null,
                "testIntroduce",
                "01012345678",
                "01098765432",
                "123@test.com",
                "서울",
                "123동",
                null
            );
        }

        private MockMultipartFile createImageFile(
            StoreRequest.StoreCreateRequest request
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
        @DisplayName("스토어 등록 요청하면 스토어 데이터를 반환한다.")
        void success_registerStore() throws Exception {

            // given
            Long sellerId = 1L;

            StoreRequest.StoreCreateRequest request = createRequest("testStore");
            MockMultipartFile requestPart = createImageFile(request);
            MockMultipartFile profileImage = createProfileFile();

            StoreResponse.StoreRegisterResult response = StoreResponse.StoreRegisterResult.builder()
                .sellerId(sellerId)
                .store(null)
                .build();

            given(sellerStoreFacade.registerStoreForSeller(
                anyLong(),
                any(StoreRequest.StoreCreateRequest.class),
                any(MultipartFile.class)
            )).willReturn(response);

            // when & then
            mockMvc.perform(
                    multipart(SellerApiPath.PREFIX + "/stores")
                        .file(requestPart)
                        .file(profileImage)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sellerId").value(sellerId));
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("스토어명이 비어있는 경우 스토어 등록 요청에 실패한다.")
        void fail_RegisterStoreTest_invalid_request() throws Exception {

            // given
            StoreRequest.StoreCreateRequest request = createRequest("");
            MockMultipartFile requestPart = createImageFile(request);
            MockMultipartFile profileImage = createProfileFile();

            // when & then
            mockMvc.perform(
                    multipart(SellerApiPath.PREFIX + "/stores")
                        .file(requestPart)
                        .file(profileImage)
                )
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockAuthenticationPrincipal(role = "SELLER")
        @DisplayName("이미 스토어를 등록한 판매자인 경우 스토어 등록 요청에 실패한다.")
        void fail_RegisterStoreTest_already_registered() throws Exception {

            // given
            StoreRequest.StoreCreateRequest request = createRequest("testStore");
            MockMultipartFile requestPart = createImageFile(request);
            MockMultipartFile profileImage = createProfileFile();

            given(sellerStoreFacade.registerStoreForSeller(anyLong(), any(), any()))
                .willThrow(new BbangleException(BbangleErrorCode.ALREADY_REGISTER_STORE));

            // when & then
            mockMvc.perform(
                    multipart(SellerApiPath.PREFIX + "/stores")
                        .file(requestPart)
                        .file(profileImage)
                )
                .andExpect(status().isBadRequest());
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
            mockMvc.perform(get(SellerApiPath.PREFIX  + "/stores" + "/search")
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
            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores" + "/search")
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

            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores" + "/search")
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
            given(sellerStoreService.checkStoreName(storeName)).willReturn(response);

            // then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores" + "/check-name")
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
        @DisplayName("조회한 스토어의 상태가 None일 경우 사용 가능하다.")
        void success_checkStoreNameDuplicate_statusNone_available() throws Exception {

            // given
            String storeName = "빵";
            StoreResponse.SellerStoreDetail sellerStoreDetail =
                new SellerStoreDetail(1L, "빵긋", "테스트", "test.png", StoreStatus.NONE, "01012345678", "01098765432", "123@test.com", "서울", "123동");
            StoreResponse.StoreNameCheck response = StoreNameCheck.builder()
                .available(true)
                .store(sellerStoreDetail)
                .build();

            // when
            given(sellerStoreService.checkStoreName(storeName)).willReturn(response);

            // then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/stores" + "/check-name")
                    .param("storeName", storeName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
                .andExpect(jsonPath("$.result.available").value(true))
                .andExpect(jsonPath("$.result.store.storeId").value(1L))
                .andExpect(jsonPath("$.result.store.name").value("빵긋"))
                .andExpect(jsonPath("$.result.store.introduce").value("테스트"))
                .andExpect(jsonPath("$.result.store.profile").value("test.png"))
                .andExpect(jsonPath("$.result.store.status").value("NONE"));
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("조회한 스토어의 상태가 None이 아닐 경우 사용할 수 없다.")
        void success_checkStoreNameDuplicate_statusNotNone_unavailable() throws Exception {

            // given
            String storeName = "빵";
            StoreResponse.SellerStoreDetail sellerStoreDetail =
                new SellerStoreDetail(1L, "빵긋", "테스트", "test.png", StoreStatus.ACTIVE, "01012345678", "01098765432", "123@test.com", "서울", "123동");
            StoreResponse.StoreNameCheck response = StoreNameCheck.builder()
                .available(false)
                .store(sellerStoreDetail)
                .build();

            // when
            given(sellerStoreService.checkStoreName(storeName)).willReturn(response);

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
                .andExpect(jsonPath("$.result.store.profile").value("test.png"))
                .andExpect(jsonPath("$.result.store.status").value("ACTIVE"));
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
}