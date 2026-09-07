package com.bbangle.bbangle.member.customer.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.member.customer.controller.dto.request.DeliveryAddressSaveRequest;
import com.bbangle.bbangle.member.customer.controller.dto.response.DeliveryAddressResponse;
import com.bbangle.bbangle.member.customer.service.DeliveryAddressService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer/delivery-addresses")
public class DeliveryAddressController {

    private final DeliveryAddressService deliveryAddressService;
    private final ResponseService responseService;

    @GetMapping
    @Operation(summary = "배송지 목록 조회")
    public ListResult<DeliveryAddressResponse> getDeliveryAddresses(
        @AuthenticationPrincipal Long memberId
    ) {
        return responseService.getListResult(
            deliveryAddressService.getDeliveryAddresses(memberId)
        );
    }

    @PostMapping
    @Operation(summary = "배송지 추가")
    public SingleResult<DeliveryAddressResponse> addDeliveryAddress(
        @Valid @RequestBody DeliveryAddressSaveRequest request,
        @AuthenticationPrincipal Long memberId
    ) {
        return responseService.getSingleResult(
            deliveryAddressService.addDeliveryAddress(memberId, request)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "배송지 수정")
    public SingleResult<DeliveryAddressResponse> updateDeliveryAddress(
        @PathVariable Long id,
        @Valid @RequestBody DeliveryAddressSaveRequest request,
        @AuthenticationPrincipal Long memberId
    ) {
        return responseService.getSingleResult(
            deliveryAddressService.updateDeliveryAddress(memberId, id, request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "배송지 삭제")
    public CommonResult deleteDeliveryAddress(
        @PathVariable Long id,
        @AuthenticationPrincipal Long memberId
    ) {
        deliveryAddressService.deleteDeliveryAddress(memberId, id);
        return responseService.getSuccessResult();
    }

    @PatchMapping("/{id}/default")
    @Operation(summary = "기본 배송지 설정")
    public CommonResult setDefaultDeliveryAddress(
        @PathVariable Long id,
        @AuthenticationPrincipal Long memberId
    ) {
        deliveryAddressService.setDefaultDeliveryAddress(memberId, id);
        return responseService.getSuccessResult();
    }
}
