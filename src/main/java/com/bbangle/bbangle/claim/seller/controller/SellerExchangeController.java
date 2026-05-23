package com.bbangle.bbangle.claim.seller.controller;

import com.bbangle.bbangle.claim.seller.controller.dto.ExchangeDecisionRequest;
import com.bbangle.bbangle.claim.seller.controller.dto.ExchangeInvoiceRequest;
import com.bbangle.bbangle.claim.seller.controller.dto.ExchangeInvoiceUpdateRequest;
import com.bbangle.bbangle.claim.seller.controller.swagger.SellerExchangeApi;
import com.bbangle.bbangle.claim.seller.service.SellerExchangeService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/exchanges")
@RestController
public class SellerExchangeController implements SellerExchangeApi {

    private final ResponseService responseService;
    private final SellerExchangeService sellerExchangeService;

    @PostMapping("/{exchangeId}/process")
    public CommonResult processExchange(
        @PathVariable Long exchangeId,
        @Valid @RequestBody ExchangeDecisionRequest request,
        @AuthenticationPrincipal Long sellerId
    ) {
        sellerExchangeService.processExchange(exchangeId, sellerId, request.decisionType(), request.reason());
        return responseService.getSuccessResult();
    }

    @PatchMapping("/{exchangeId}/invoice")
    public CommonResult registerExchangeInvoice(
        @PathVariable Long exchangeId,
        @Valid @RequestBody ExchangeInvoiceRequest request,
        @AuthenticationPrincipal Long sellerId
    ) {
        sellerExchangeService.registerExchangeInvoice(exchangeId, sellerId, request.courierCode(), request.trackingNumber());
        return responseService.getSuccessResult();
    }

    @PutMapping("/{exchangeId}/invoice")
    public CommonResult updateExchangeInvoice(
        @PathVariable Long exchangeId,
        @Valid @RequestBody ExchangeInvoiceUpdateRequest request,
        @AuthenticationPrincipal Long sellerId
    ) {
        sellerExchangeService.updateExchangeInvoice(exchangeId, sellerId, request.courierCode(), request.trackingNumber());
        return responseService.getSuccessResult();
    }

    @PostMapping("/{exchangeId}/complete")
    public CommonResult completeExchange(
        @PathVariable Long exchangeId,
        @AuthenticationPrincipal Long sellerId
    ) {
        sellerExchangeService.completeExchange(exchangeId, sellerId);
        return responseService.getSuccessResult();
    }
}
