package com.bbangle.bbangle.vat.seller.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.vat.seller.controller.dto.request.SellerVatSearchRequest;
import com.bbangle.bbangle.vat.seller.controller.dto.response.SellerVatSummaryResponse;
import com.bbangle.bbangle.vat.seller.service.SellerVatService;
import com.bbangle.bbangle.vat.seller.service.model.SellerVatCommand.SellerVatSearchCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/vat")
public class SellerVatController implements SellerVatApi {

    private final ResponseService responseService;
    private final SellerVatService sellerVatService;

    @Override
    @GetMapping
    public SingleResult<SellerVatSummaryResponse> getSellerVatSummary(
        SellerVatSearchRequest request,
        @AuthenticationPrincipal Long sellerId
    ) {
        SellerVatSearchCommand command = request.toCommand(sellerId);
        SellerVatSummaryResponse response = sellerVatService.getVatSummary(command);

        return responseService.getSingleResult(response);
    }
}
