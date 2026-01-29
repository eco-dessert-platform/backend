package com.bbangle.bbangle.store.seller.controller;

import static com.bbangle.bbangle.common.service.ResponseService.CommonResponse.SUCCESS;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreDetail;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreNameCheck;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
    private MockMvc mockMvc;

    @MockBean
    private SellerStoreService sellerStoreService;

    @SpyBean
    private ResponseService responseService;

    @Test
    @WithMockUser(roles = "SELLER")
    @DisplayName("스토어 이름으로 스토어 목록을 반환한다.")
    void success_search() throws Exception {

        // given
        String storeName = "빵";
        StoreInfo storeInfo = StoreInfo.builder()
            .id(1L)
            .name("빵굽는하루")
            .status(StoreStatus.NONE)
            .build();

        CursorPagination<SellerStoreInfo.StoreInfo> pagination = CursorPagination.of(List.of(storeInfo),20, null, StoreInfo::id);
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
            .andExpect(jsonPath("$.result.content[0].status").value("NONE"))
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
            .status(StoreStatus.NONE)
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
            .andExpect(jsonPath("$.result.content[0].status").value("NONE"))
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
            new SellerStoreDetail(1L, "빵긋", "테스트", "test.png", StoreStatus.NONE);
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
            new SellerStoreDetail(1L, "빵긋", "테스트", "test.png", StoreStatus.ACTIVE);
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