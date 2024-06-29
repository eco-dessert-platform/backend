package com.bbangle.bbangle.push.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.push.dto.CreatePushRequest;
import com.bbangle.bbangle.push.dto.PushRequest;
import com.bbangle.bbangle.push.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
public class PushController {

    private final PushService pushService;
    private final ResponseService responseService;


    @PostMapping
    public CommonResult createPush(
            @Validated @RequestBody CreatePushRequest request
//            @AuthenticationPrincipal Long memberId
    ) {
        Long memberId = 1L;
        pushService.createPush(request, memberId);
        return responseService.getSuccessResult();
    }


    @PatchMapping
    public CommonResult cancelPush(
            @Validated @RequestBody PushRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        pushService.cancelPush(request, memberId);
        return responseService.getSuccessResult();
    }


    @DeleteMapping
    public CommonResult deletePush(
            @Validated @RequestBody PushRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        pushService.deletePush(request, memberId);
        return responseService.getSuccessResult();
    }


    @GetMapping
    public CommonResult getPush(
            @RequestParam(value = "pushCategory") String pushCategory
//            @AuthenticationPrincipal Long memberId
    ) {
        Long memberId = 1L;
        return responseService.getListResult(pushService.getPush(pushCategory, memberId));
    }

}
