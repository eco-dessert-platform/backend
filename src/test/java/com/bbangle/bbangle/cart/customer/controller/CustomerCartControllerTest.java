package com.bbangle.bbangle.cart.customer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest;
import com.bbangle.bbangle.cart.customer.facade.CustomerCartFacade;
import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.client.annotation.WithMockAuthenticationPrincipal;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.CustomerApiPath;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("[컨트롤러 테스트] CustomerCartController")
@WebMvcTest(controllers = CustomerCartController.class)
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@ActiveProfiles("test")
class CustomerCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerCartFacade customerCartFacade;

    @SpyBean
    private ResponseService responseService;

    @Nested
    @DisplayName("addCart() 테스트")
    class AddCartTest {

        @Test
        @WithMockAuthenticationPrincipal()
        @DisplayName("장바구니 추가에 성공한다.")
        void success_add_cart() throws Exception {

            // given
            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                1L,
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(
                        1L,
                        3
                    )
                )
            );

            // when & then
            mockMvc.perform(post(CustomerApiPath.PREFIX + "/carts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("SUCCESS"));

            verify(customerCartFacade).addCartItem(eq(1L), any(CartRequest.AddCartRequest.class));
        }

        @Test
        @WithMockAuthenticationPrincipal()
        @DisplayName("상품 옵션이 존재하지 않으면 예외를 반환한다.")
        void fail_add_cart_product_not_found() throws Exception {

            // given
            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                1L,
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(
                        999L,
                        3
                    )
                )
            );

            willThrow(new BbangleException(BbangleErrorCode.PRODUCT_NOT_FOUND))
                .given(customerCartFacade)
                .addCartItem(anyLong(), any(CartRequest.AddCartRequest.class));

            // when & then
            mockMvc.perform(post(CustomerApiPath.PREFIX + "/carts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(BbangleErrorCode.PRODUCT_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(BbangleErrorCode.PRODUCT_NOT_FOUND.getMessage()));

            verify(customerCartFacade).addCartItem(eq(1L), any(CartRequest.AddCartRequest.class));
        }
    }
}