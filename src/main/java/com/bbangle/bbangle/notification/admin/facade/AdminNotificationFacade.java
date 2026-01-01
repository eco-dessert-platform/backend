package com.bbangle.bbangle.notification.admin.facade;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.service.AdminNotificationService;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Slf4j
@Service
public class AdminNotificationFacade {

     private final S3Service s3Service;
     private final AdminNotificationService adminNotificationService;

    private static final String ADMIN_NOTICE_FOLDER = "admin-notice-images";

    public NoticeInfo createNotice(Long adminId, AdminNotificationCreateRequest request, List<MultipartFile> profileImage) {
        // 이미지 저장
        List<String> imageLinks = s3Service.saveMultipleAndReturnWithCdn(ADMIN_NOTICE_FOLDER, profileImage);
        try {
            // 공지사항 생성
            return adminNotificationService.createAdminNotification(request.toCreateCommand(adminId, imageLinks));
        } catch (Exception e) {
            log.error(e.getMessage());
            log.debug("이미지 업로드 되어진 이미지 삭제 진행");
            s3Service.deleteImagesCdn(imageLinks);
            throw new BbangleException(BbangleErrorCode.ADMIN_NOTICE_CREATION_FAILED);
        }
    }

}
