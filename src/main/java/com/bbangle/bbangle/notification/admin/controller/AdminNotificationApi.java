package com.bbangle.bbangle.notification.admin.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationUpdateRequest;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationCreateResponse;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationDeleteResponse;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationDetailResponse;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationSearchResponse;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Notification", description = "(관리자) 공지사항 API")
public interface AdminNotificationApi {

    @Operation(summary = "(관리자) 공지사항 등록")
    SingleResult<AdminNotificationCreateResponse> registerNotification(
        Long adminId,
        AdminNotificationCreateRequest request,
        List<MultipartFile> profileImage
    );

    @Operation(summary = "(관리자) 공지사항 수정")
    SingleResult<AdminNotificationUpdateResponse> updateNotification(
        Long adminId,
        Long noticeId,
        AdminNotificationUpdateRequest request,
        List<MultipartFile> profileImage
    );

    @Operation(summary = "(관리자) 공지사항 조회")
    SingleResult<BbanglePageResponse<AdminNotificationSearchResponse>> searchNotification(
        @ParameterObject Pageable pageable);

    @Operation(summary = "(관리자) 공지사항 단건 조회")
    SingleResult<AdminNotificationDetailResponse> getNotification(Long noticeId);

    @Operation(summary = "(관리자) 공지사항 삭제")
    SingleResult<AdminNotificationDeleteResponse> deleteNotification(
        Long adminId,
        List<Long> noticeId);

}
