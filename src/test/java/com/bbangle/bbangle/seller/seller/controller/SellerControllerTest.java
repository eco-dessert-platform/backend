package com.bbangle.bbangle.seller.seller.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.client.annotation.WithMockAuthenticationPrincipal;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerResponse;
import com.bbangle.bbangle.seller.seller.facade.SellerFacade;
import com.bbangle.bbangle.seller.seller.service.AccountVerificationService;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("[컨트롤러 테스트] SellerController")
@WebMvcTest(controllers = SellerController.class)
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@ActiveProfiles("test")
class SellerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResponseService responseService;

    @MockBean
    private SellerService sellerService;

    @MockBean
    private SellerFacade sellerFacade;

    @MockBean
    private AccountVerificationService accountVerificationService;

    @Nested
    @DisplayName("getRegisteredStoreDetail() 테스트")
    class GetRegisteredStoreDetailTest {

        @Test
        @DisplayName("등록 신청한 스토어가 존재할 경우 해당 스토어 정보를 조회한다.")
        @WithMockAuthenticationPrincipal(role = "SELLER")
        void getRegisteredStoreDetail_exist_registeredStore() throws Exception {

            // given
            Long sellerId = 1L;
            StoreResponse.SellerStoreDetail sellerStoreDetail =
                new SellerStoreDetail(1L, "빵긋", "테스트", "test.png", StoreStatus.RESERVED, "01012345678", "01098765432", "123@test.com", "서울", "123동");

            SellerResponse.RegisteredStoreDetail response = SellerResponse.RegisteredStoreDetail.builder()
                .sellerId(1L)
                .store(sellerStoreDetail)
                .build();

            given(sellerFacade.getRegisteredStoreDetail(sellerId)).willReturn(response);

            // when & then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sellerId").value(sellerId))
                .andExpect(jsonPath("$.result.store.storeId").value(sellerStoreDetail.storeId()))
                .andExpect(jsonPath("$.result.store.name").value(sellerStoreDetail.name()))
                .andExpect(jsonPath("$.result.store.introduce").value(sellerStoreDetail.introduce()))
                .andExpect(jsonPath("$.result.store.profile").value(sellerStoreDetail.profile()))
                .andExpect(jsonPath("$.result.store.status").value(sellerStoreDetail.status().name()))
                .andExpect(jsonPath("$.result.store.phoneNumber").value(sellerStoreDetail.phoneNumber()))
                .andExpect(jsonPath("$.result.store.subPhoneNumber").value(sellerStoreDetail.subPhoneNumber()))
                .andExpect(jsonPath("$.result.store.email").value(sellerStoreDetail.email()))
                .andExpect(jsonPath("$.result.store.originAddress").value(sellerStoreDetail.originAddress()))
                .andExpect(jsonPath("$.result.store.originAddressDetail").value(sellerStoreDetail.originAddressDetail()));

            verify(sellerFacade).getRegisteredStoreDetail(sellerId);
        }

        @Test
        @DisplayName("등록 신청한 스토어가 없을 경우 null을 반환한다.")
        @WithMockAuthenticationPrincipal(role = "SELLER")
        void getRegisteredStoreDetail_noExist_registeredStore() throws Exception {

            // given
            Long sellerId = 1L;

            SellerResponse.RegisteredStoreDetail response = SellerResponse.RegisteredStoreDetail.builder()
                .sellerId(1L)
                .store(null)
                .build();

            given(sellerFacade.getRegisteredStoreDetail(sellerId)).willReturn(response);

            // when & then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sellerId").value(sellerId))
                .andExpect(jsonPath("$.result.store").isEmpty());

            verify(sellerFacade).getRegisteredStoreDetail(sellerId);
        }
    }
}