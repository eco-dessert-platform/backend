package com.bbangle.bbangle.notification.admin.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.AdminApiPath;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationUpdateRequest;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationCreateResponse;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationUpdateResponse;
import com.bbangle.bbangle.notification.admin.facade.AdminNotificationFacade;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(AdminApiPath.PREFIX + "/notifications")
public class AdminNotificationController implements AdminNotificationApi {

    private final ResponseService responseService;
    private final AdminNotificationFacade adminNotificationFacade;

    @PostMapping(value= "/{adminId}/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Override
    public SingleResult<AdminNotificationCreateResponse> registerNotification(@PathVariable Long adminId,
                                                                              @RequestPart @Valid AdminNotificationCreateRequest request,
                                                                              @RequestPart(required = false) List<MultipartFile> profileImage) {

        NoticeInfo noticeInfo = adminNotificationFacade.createNotice(adminId, request, profileImage);
        AdminNotificationCreateResponse response = AdminNotificationCreateResponse.from(noticeInfo);

        return responseService.getSingleResult(response);
    }

    @PutMapping(value= "/{noticeId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Override
    public SingleResult<AdminNotificationUpdateResponse> updateNotification(@RequestParam Long adminId,
                                                                            @PathVariable Long noticeId,
                                                                            @RequestPart @Valid AdminNotificationUpdateRequest request,
                                                                            @RequestPart(required = false) List<MultipartFile> profileImage) {

        NoticeInfo noticeInfo = adminNotificationFacade.updateNotice(adminId, noticeId, request, profileImage);
        AdminNotificationUpdateResponse response = AdminNotificationUpdateResponse.from(noticeInfo);

        return responseService.getSingleResult(response);
    }
}
