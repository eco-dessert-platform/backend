package com.bbangle.bbangle.notification.customer.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.NotificationCustomPage;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.notification.customer.dto.NotificationDetailResponseDto;
import com.bbangle.bbangle.notification.customer.dto.NotificationResponse;
import com.bbangle.bbangle.notification.customer.dto.NotificationUploadRequest;
import com.bbangle.bbangle.notification.customer.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "공지사항 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final ResponseService responseService;

    @Operation(summary = "공지사항 목록 조회")
    @GetMapping
    public SingleResult<NotificationCustomPage<List<NotificationResponse>>> getList(
        @RequestParam(required = false, value = "cursorId") @Parameter(description = "커서 ID")
        Long cursorId
    ) {
        return responseService.getSingleResult(notificationService.getList(cursorId));
    }

    @Operation(summary = "공지사항 상세 조회")
    @GetMapping("/{id}")
    public SingleResult<NotificationDetailResponseDto> getNoticeDetail(
        @PathVariable @Parameter(description = "공지사항 ID", example = "1")
        Long id
    ) {
        return responseService.getSingleResult(notificationService.getNoticeDetail(id));
    }

    @Operation(summary = "공지사항 등록")
    @PostMapping
    public CommonResult upload(
        @RequestBody @Valid
        NotificationUploadRequest notificationUploadRequest
    ) {
        notificationService.upload(notificationUploadRequest);
        return responseService.getSuccessResult();
    }
}
