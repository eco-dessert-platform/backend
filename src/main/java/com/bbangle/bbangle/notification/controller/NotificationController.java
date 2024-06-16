package com.bbangle.bbangle.notification.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.notification.dto.NotificationUploadRequest;
import com.bbangle.bbangle.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final ResponseService responseService;

    @GetMapping
    public CommonResult getList(
        @RequestParam(required = false, value = "cursorId")
        Long cursorId
    ) {
        return responseService.getSingleResult(notificationService.getList(cursorId));
    }

    @GetMapping("/{id}")
    public CommonResult getNoticeDetail(
        @PathVariable
        Long id
    ) {
        return responseService.getSingleResult(notificationService.getNoticeDetail(id));
    }

    @PostMapping
    public CommonResult upload(
        @RequestBody
        NotificationUploadRequest notificationUploadRequest
    ) {
        notificationService.upload(notificationUploadRequest);
        return responseService.getSuccessResult();
    }
}
