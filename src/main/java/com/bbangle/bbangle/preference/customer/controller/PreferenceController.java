package com.bbangle.bbangle.preference.customer.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.preference.customer.dto.MemberPreferenceResponse;
import com.bbangle.bbangle.preference.customer.dto.PreferenceSelectRequest;
import com.bbangle.bbangle.preference.customer.dto.PreferenceUpdateRequest;
import com.bbangle.bbangle.preference.customer.service.PreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/preference")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;
    private final ResponseService responseService;

    @PostMapping
    @Operation(summary = "사용자 취향 등록")
    public CommonResult select(
            @RequestBody
            PreferenceSelectRequest request,
            @AuthenticationPrincipal
            Long memberId
    ) {
        preferenceService.register(request, memberId);
        return responseService.getSuccessResult();
    }

    @PutMapping
    @Operation(summary = "사용자 취향 수정")
    public CommonResult update(
            @RequestBody
            PreferenceUpdateRequest request,
            @AuthenticationPrincipal
            Long memberId
    ) {
        preferenceService.update(request, memberId);
        return responseService.getSuccessResult();
    }

    @GetMapping
    @Operation(summary = "사용자 취향 조회")
    public SingleResult<MemberPreferenceResponse> getPreference(
            @AuthenticationPrincipal
            Long memberId
    ) {
        MemberPreferenceResponse response = preferenceService.getPreference(memberId);
        return responseService.getSingleResult(response);
    }
}
