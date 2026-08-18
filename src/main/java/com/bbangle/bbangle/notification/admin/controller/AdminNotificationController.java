package com.bbangle.bbangle.notification.admin.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.AdminApiPath;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationUpdateRequest;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationCreateResponse;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationDeleteResponse;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationDetailResponse;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationSearchResponse;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationUpdateResponse;
import com.bbangle.bbangle.notification.admin.facade.AdminNotificationFacade;
import com.bbangle.bbangle.notification.admin.service.AdminNotificationService;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(AdminApiPath.PREFIX + "/notifications")
public class AdminNotificationController implements AdminNotificationApi {

    private final ResponseService responseService;
    private final AdminNotificationFacade adminNotificationFacade;
    private final AdminNotificationService adminNotificationService;

    @PostMapping(value = "/{adminId}/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Override
    public SingleResult<AdminNotificationCreateResponse> registerNotification(@PathVariable Long adminId,
                                                                              @RequestPart @Valid AdminNotificationCreateRequest request,
                                                                              @RequestPart(required = false) List<MultipartFile> profileImage) {

        NoticeInfo noticeInfo = adminNotificationFacade.createNotice(adminId, request, profileImage);
        AdminNotificationCreateResponse response = AdminNotificationCreateResponse.from(noticeInfo);

        return responseService.getSingleResult(response);
    }

    @PutMapping(value = "/{noticeId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Override
    public SingleResult<AdminNotificationUpdateResponse> updateNotification(@AuthenticationPrincipal Long adminId,
                                                                            @PathVariable Long noticeId,
                                                                            @RequestPart @Valid AdminNotificationUpdateRequest request,
                                                                            @RequestPart(required = false) List<MultipartFile> profileImage) {

        NoticeInfo noticeInfo = adminNotificationFacade.updateNotice(adminId, noticeId, request, profileImage);
        AdminNotificationUpdateResponse response = AdminNotificationUpdateResponse.from(noticeInfo);

        return responseService.getSingleResult(response);
    }

    @GetMapping
    @Override
    public SingleResult<BbanglePageResponse<AdminNotificationSearchResponse>> searchNotification(
        @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Direction.DESC)
        Pageable pageable
    ) {
        Page<NoticeInfo> result = adminNotificationService.searchNotice(pageable);
        return responseService.getSingleResult(
            BbanglePageResponse.of(result.map(AdminNotificationSearchResponse::from)));
    }

    @GetMapping("/{noticeId}")
    @Override
    public SingleResult<AdminNotificationDetailResponse> getNotification(@PathVariable Long noticeId) {
        NoticeInfo noticeInfo = adminNotificationService.getNotice(noticeId);
        AdminNotificationDetailResponse response = AdminNotificationDetailResponse.from(noticeInfo);

        return responseService.getSingleResult(response);
    }

    @DeleteMapping
    @Override
    public SingleResult<AdminNotificationDeleteResponse> deleteNotification(
        @AuthenticationPrincipal Long adminId,
        @RequestBody List<Long> noticeId) {

        AdminNotificationDeleteResponse response = adminNotificationService.deleteNotification(adminId, noticeId);
        return responseService.getSingleResult(response);
    }
}
