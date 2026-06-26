package com.bbangle.bbangle.cart.customer.controller;

import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest;
import com.bbangle.bbangle.cart.customer.controller.dto.CartResponse.CartListResponse;
import com.bbangle.bbangle.cart.customer.controller.swagger.CustomerCartApi;
import com.bbangle.bbangle.cart.customer.facade.CustomerCartFacade;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.CustomerApiPath;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(CustomerApiPath.PREFIX + "/carts")
public class CustomerCartController implements CustomerCartApi {

    private final ResponseService responseService;
    private final CustomerCartFacade customerCartFacade;

    @Override
    @PostMapping
    public CommonResult addCart(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody CartRequest.AddCartRequest request
    ) {
        customerCartFacade.addCartItem(memberId, request);
        return responseService.getSuccessResult();
    }

    @Override
    @GetMapping
    public SingleResult<CartListResponse> getCart(
        @AuthenticationPrincipal Long memberId) {

        return responseService.getSingleResult(customerCartFacade.getCart(memberId));
    }
}
