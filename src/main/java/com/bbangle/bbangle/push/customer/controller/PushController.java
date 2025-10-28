package com.bbangle.bbangle.push.customer.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.push.customer.dto.CreatePushRequest;
import com.bbangle.bbangle.push.customer.dto.FcmRequest;
import com.bbangle.bbangle.push.customer.dto.FcmTestDto;
import com.bbangle.bbangle.push.customer.dto.PushRequest;
import com.bbangle.bbangle.push.customer.service.FcmService;
import com.bbangle.bbangle.push.customer.service.PushService;
import com.bbangle.bbangle.push.domain.PushCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pushes", description = "빵켓팅/재입고 알림 API")
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
        @RequestParam(value = "pushCategory")
        @Schema(description = "푸시 카테고리 타입")
        PushCategory pushCategory,
        @AuthenticationPrincipal Long memberId
    ) {
        return responseService.getListResult(pushService.getPushes(pushCategory, memberId));
    }

    @GetMapping("/test")
    public CommonResult test() {
        List<FcmRequest> requestList = pushService.getPushesForNotification();
        fcmService.sendMessage(requestList);
        return responseService.getSuccessResult();
    }

    @PostMapping("/test")
    @Operation(summary = "알림 테스트 용 API (테스트 완료 후 삭제 예정)")
    public CommonResult sendPush(
        @RequestBody FcmTestDto fcmTestDto
    ) {
        fcmService.sendTest(fcmTestDto);
        return responseService.getSuccessResult();
    }
}
