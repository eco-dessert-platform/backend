package com.bbangle.bbangle.board.seller.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardDetailResponse;
import com.bbangle.bbangle.board.seller.facade.SellerBoardFacade;
import com.bbangle.bbangle.board.seller.service.SellerBoardService;
import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.client.annotation.WithMockAuthenticationPrincipal;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.seller.controller.dto.response.SellerBoardResponseFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("[컨트롤러 테스트] SellerBoardController")
@WebMvcTest(controllers = SellerBoardController.class)
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@ActiveProfiles("test")
class SellerBoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SellerBoardFacade sellerBoardFacade;

    @MockBean
    private SellerBoardService sellerBoardService;

    @SpyBean
    private ResponseService responseService;

    @Nested
    @DisplayName("getBoardDetail() 테스트")
    class GetBoardDetailTest {

        @Test
        @DisplayName("게시글 상세 정보를 조회하면 200과 함께 상세 응답을 반환한다.")
        @WithMockAuthenticationPrincipal(role = "SELLER")
        void success_getBoardDetail() throws Exception {

            // given
            Long boardId = 1L;
            SellerBoardDetailResponse response = SellerBoardResponseFixture.defaultResponse(boardId);

            given(sellerBoardService.getBoardDetail(1L, boardId)).willReturn(response);

            // when & then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/boards/{boardId}", boardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.boardDetailDTO.boardId").value(boardId))
                .andExpect(jsonPath("$.result.boardDetailDTO.name").value("글루텐 프리 케이크"))
                .andExpect(jsonPath("$.result.boardDetailDTO.price.base").value(10000))
                .andExpect(jsonPath("$.result.boardDetailDTO.price.discountValue").value(20))
                .andExpect(jsonPath("$.result.deliveryDTO.courier").value("CJ대한통운"))
                .andExpect(jsonPath("$.result.boardImgDTO.thumbnailImg").value("thumbnail.png"))
                .andExpect(jsonPath("$.result.boardImgDTO.additionalImgs.length()").value(2))
                .andExpect(jsonPath("$.result.options.length()").value(1))
                .andExpect(jsonPath("$.result.options[0].optionName").value("기본 옵션"))
                .andExpect(jsonPath("$.result.options[0].tags[0]").value("GLUTEN_FREE"))
                .andExpect(jsonPath("$.result.productInfoNotice.productName").value("빵그리의 식빵"));

            verify(sellerBoardService).getBoardDetail(1L, boardId);
        }

        @Test
        @DisplayName("게시글이 존재하지 않으면 404를 반환한다.")
        @WithMockAuthenticationPrincipal(role = "SELLER")
        void fail_boardNotFound() throws Exception {

            // given
            Long boardId = 999L;

            given(sellerBoardService.getBoardDetail(anyLong(), anyLong())).willThrow(new BbangleException(BbangleErrorCode.BOARD_NOT_FOUND));

            // when & then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/boards/{boardId}", boardId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BbangleErrorCode.BOARD_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(BbangleErrorCode.BOARD_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("게시글의 소유자가 아니면 403을 반환한다.")
        @WithMockAuthenticationPrincipal(role = "SELLER")
        void fail_forbiddenAccess() throws Exception {

            // given
            Long boardId = 1L;

            given(sellerBoardService.getBoardDetail(anyLong(), anyLong())).willThrow(new BbangleException(BbangleErrorCode.FORBIDDEN_BOARD_ACCESS));

            // when & then
            mockMvc.perform(get(SellerApiPath.PREFIX + "/boards/{boardId}", boardId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BbangleErrorCode.FORBIDDEN_BOARD_ACCESS.getCode()))
                .andExpect(jsonPath("$.message").value(BbangleErrorCode.FORBIDDEN_BOARD_ACCESS.getMessage()));
        }
    }
}
