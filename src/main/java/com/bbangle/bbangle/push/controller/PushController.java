package com.bbangle.bbangle.push.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.push.dto.PushRequest;
import com.bbangle.bbangle.push.dto.CreatePushRequest;
import com.bbangle.bbangle.push.dto.SendPushRequest;
import com.bbangle.bbangle.push.service.FcmService;
import com.bbangle.bbangle.push.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
public class PushController {

    private final FcmService fcmService;
    private final PushService pushService;
    private final ResponseService responseService;


    @PostMapping
    public CommonResult createPush(
            @Validated @RequestBody CreatePushRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        System.out.println("requestDto = " + request);
        pushService.createPush(request, memberId);
        return responseService.getSuccessResult();
    }


    @PatchMapping("/again")
    public CommonResult recreatePush(
            @Validated @RequestBody PushRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        System.out.println("requestDto = " + request);
        pushService.recreatePush(request, memberId);
        return responseService.getSuccessResult();
    }


    @PatchMapping
    public CommonResult cancelPush(
            @Validated @RequestBody PushRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        System.out.println("requestDto = " + request);
        pushService.cancelPush(request, memberId);
        return responseService.getSuccessResult();
    }


    @DeleteMapping
    public CommonResult deletePush(
            @Validated @RequestBody PushRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
        System.out.println("requestDto = " + request);
        pushService.deletePush(request, memberId);
        return responseService.getSuccessResult();
    }


    @GetMapping
    public CommonResult getPush(
            @RequestParam String productId,
            @AuthenticationPrincipal Long memberId
    ) {

        return null;
    }


    @PostMapping("/push")
    public CommonResult sendPush(@RequestBody SendPushRequest request) {
        System.out.println("requestDto = " + request);

        fcmService.sendPush(request);
        return responseService.getSuccessResult();
    }

}
