package com.bbangle.bbangle.push.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.push.dto.CreatePushRequest;
import com.bbangle.bbangle.push.dto.FcmRequest;
import com.bbangle.bbangle.push.dto.PushRequest;
import com.bbangle.bbangle.push.service.FcmService;
import com.bbangle.bbangle.push.service.PushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
@Slf4j
public class PushController {

    private final FcmService fcmService;
    private final PushService pushService;
    private final ResponseService responseService;


    @PostMapping
    public CommonResult createPush(
            @Validated @RequestBody CreatePushRequest request,
            @AuthenticationPrincipal Long memberId
    ) {
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
    public CommonResult getPushes(
            @RequestParam(value = "pushCategory") String pushCategory,
            @AuthenticationPrincipal Long memberId
    ) {
        return responseService.getListResult(pushService.getPushes(pushCategory, memberId));
    }

    /**
    *
    * 포스트맨에서 FCM 메세지 전송 테스트 하기 위한 용도로 구현한 임시 컨트롤러 입니다(삭제 예정)
    * */
    @PostMapping("/test")
    public CommonResult sendPush(
            @RequestBody FcmRequest fcmRequest
    ) {
        try {
            fcmService.sendMessageTo(fcmRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return responseService.getSuccessResult();
    }

}
