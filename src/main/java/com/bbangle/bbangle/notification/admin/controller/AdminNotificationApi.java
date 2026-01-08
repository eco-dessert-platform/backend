package com.bbangle.bbangle.notification.admin.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Notification", description = "(관리자) 공지사항 API")
public interface AdminNotificationApi {

    @Operation(summary = "(관리자) 공지사항 등록")
    SingleResult<AdminNotificationCreateResponse> registerNotification(
        Long adminId,
        AdminNotificationCreateRequest request,
        List<MultipartFile> profileImage
    );
}
