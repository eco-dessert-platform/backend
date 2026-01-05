package com.bbangle.bbangle.notification.admin.facade;

import com.bbangle.bbangle.common.service.HtmlContentProcessor;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.service.AdminNotificationService;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
     private final HtmlContentProcessor htmlContentProcessor;

    private static final String ADMIN_NOTICE_FOLDER = "admin-notice-images";

    // 로직 흐름
    // 이미지 저장 (uuid로 매핑) -> 본문 이미지 태그 변경 (uuid -> CDN URL) -> 공지사항 생성
    public NoticeInfo createNotice(Long adminId, AdminNotificationCreateRequest request, List<MultipartFile> images) {
        Map<String, String> cdnUrlMap = new HashMap<>();
        List<String> uploadedImageLinks = new ArrayList<>();

        try {
            // 이미지 업로드 (uuid를 키로 사용)
            for (MultipartFile image : images) {
                String uuid = image.getOriginalFilename(); // 프론트에서 전달받은 uuid
                String cdnUrl = s3Service.saveAndReturnWithCdn(ADMIN_NOTICE_FOLDER, image);
                cdnUrlMap.put(uuid, cdnUrl);
                uploadedImageLinks.add(cdnUrl);
            }

            // Content에서 임시 uuid를 CDN URL로 변환
            String convertedContent = htmlContentProcessor.changeToCdn(request.content(), cdnUrlMap);

            // 공지사항 생성
            return adminNotificationService.createAdminNotification(
                request.toCreateCommand(adminId, convertedContent, uploadedImageLinks)
            );
        } catch (Exception e) {
            log.error("이미지 업로드 중 오류 발생", e);
            // 업로드된 이미지 롤백
            if (!uploadedImageLinks.isEmpty()) {
                log.debug("업로드된 이미지 삭제 진행");
                s3Service.deleteImagesCdn(uploadedImageLinks);
            }
            throw new BbangleException(BbangleErrorCode.ADMIN_NOTICE_CREATION_FAILED);
        }
    }

}
