package com.bbangle.bbangle.preference.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.preference.dto.MemberPreferenceResponse;
import com.bbangle.bbangle.preference.dto.PreferenceSelectRequest;
import com.bbangle.bbangle.preference.dto.PreferenceUpdateRequest;
import com.bbangle.bbangle.preference.service.PreferenceService;
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
    public CommonResult getPreference(
        @AuthenticationPrincipal
        Long memberId
    ) {
        MemberPreferenceResponse response = preferenceService.getPreference(memberId);
        return responseService.getSingleResult(response);
    }
}
