package com.bbangle.bbangle.notification.admin.facade;

import com.bbangle.bbangle.common.service.HtmlContentProcessor;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationCreateRequest;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationRequest.AdminNotificationUpdateRequest;
import com.bbangle.bbangle.notification.admin.service.AdminNotificationService;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final String CREATE_NOTICE = "공지사항 생성";
    private static final String UPDATE_NOTICE = "공지사항 수정";

    // 로직 흐름
    // 이미지 저장 (uuid로 매핑) -> 본문 이미지 태그 변경 (uuid -> CDN URL) -> 공지사항 생성
    public NoticeInfo createNotice(Long adminId, AdminNotificationCreateRequest request, List<MultipartFile> images) {
        List<String> uploadedImageLinks = new ArrayList<>();

        try {
            if(images == null || images.isEmpty()) {
                return adminNotificationService.createAdminNotification(
                    request.toCreateCommand(adminId, request.content()));
            }

            Map<String, String> cdnUrlMap = uploadImagesToS3(images, uploadedImageLinks);
            String convertedContent = htmlContentProcessor.changeToCdn(request.content(), cdnUrlMap);

            return adminNotificationService.createAdminNotification(
                request.toCreateCommand(adminId, convertedContent, uploadedImageLinks)
            );
        } catch (Exception e) {
            log.error("이미지 업로드 중 오류 발생", e);
            rollbackUploadedImages(uploadedImageLinks, CREATE_NOTICE);
            throw new BbangleException(BbangleErrorCode.ADMIN_NOTICE_CREATION_FAILED);
        }
    }

    public NoticeInfo updateNotice(Long adminId, Long noticeId, AdminNotificationUpdateRequest request,
                                   List<MultipartFile> images) {
        List<String> uploadedImageLinks = new ArrayList<>();

        try {
            if (images != null && !images.isEmpty()) {
                return updateNoticeWithImages(adminId, noticeId, request, images, uploadedImageLinks);
            }
            return updateNoticeWithoutImages(adminId, noticeId, request);
        } catch (Exception e) {
            log.error("공지사항 수정 중 오류 발생 {} ", e.getMessage());
            rollbackUploadedImages(uploadedImageLinks, UPDATE_NOTICE);
            throw new BbangleException(BbangleErrorCode.ADMIN_NOTICE_UPDATE_FAILED);
        }
    }


    private NoticeInfo updateNoticeWithImages(Long adminId, Long noticeId, AdminNotificationUpdateRequest request,
                                              List<MultipartFile> images, List<String> uploadedImageLinks) {
        // 이미지 업로드 진행
        Map<String, String> cdnUrlMap = uploadImagesToS3(images, uploadedImageLinks);
        // 본문에 존재하는 이미지 링크를 cdn으로 교체
        String convertedContent = htmlContentProcessor.changeToCdn(request.content(), cdnUrlMap);
        // 전체 이미지 링크 추출
        List<String> wholeImageLinks = htmlContentProcessor.extractImageSrc(convertedContent);
        // 제거된 이미지 링크 확인
        List<String> removedImages = findRemovedImages(noticeId, wholeImageLinks);

        NoticeInfo result = adminNotificationService.updateAdminNotification(
            request.toUpdateImageCommand(adminId, noticeId, request.title(), convertedContent, wholeImageLinks)
        );
        // 제거된 이미지 링크를 가져와 s3에서 이미지 제거 진행
        deleteRemovedImagesFromS3(removedImages);

        return result;
    }

    private NoticeInfo updateNoticeWithoutImages(Long adminId, Long noticeId, AdminNotificationUpdateRequest request) {
        // 본문에서 현재 이미지 링크 추출
        List<String> currentImageLinks = htmlContentProcessor.extractImageSrc(request.content());

        // 기존 이미지와 비교하여 삭제된 이미지 찾기
        List<String> removedImages = findRemovedImages(noticeId, currentImageLinks);

        // DB 업데이트
        NoticeInfo result;
        if (!currentImageLinks.isEmpty()) {
            // 본문에 이미지가 남아있으면 imageLinks 업데이트
            result = adminNotificationService.updateAdminNotification(
                request.toUpdateImageCommand(adminId, noticeId, request.title(), request.content(), currentImageLinks)
            );
        } else {
            // 본문에 이미지가 없으면 일반 업데이트
            result = adminNotificationService.updateAdminNotification(
                request.toUpdateCommand(adminId, noticeId, request.title(), request.content())
            );
        }

        // S3에서 삭제된 이미지 제거
        deleteRemovedImagesFromS3(removedImages);

        return result;
    }

    private Map<String, String> uploadImagesToS3(List<MultipartFile> images, List<String> uploadedImageLinks) {
        Map<String, String> cdnUrlMap = new HashMap<>();

        for (MultipartFile image : images) {
            String uuid = image.getOriginalFilename();
            String cdnUrl = s3Service.saveAndReturnWithCdn(ADMIN_NOTICE_FOLDER, image);
            cdnUrlMap.put(uuid, cdnUrl);
            uploadedImageLinks.add(cdnUrl);
        }

        return cdnUrlMap;
    }

    private List<String> findRemovedImages(Long noticeId, List<String> newImageLinks) {
        List<String> existingImages = adminNotificationService.getExistingImageLinks(noticeId);
        Set<String> newImageSet = new HashSet<>(newImageLinks);

        return existingImages.stream()
            .filter(img -> !newImageSet.contains(img))
            .toList();
    }

    // 공지사항에서 제거된 이미지를 S3에서 삭제
    private void deleteRemovedImagesFromS3(List<String> removedImages) {
        if (removedImages.isEmpty()) {
            return;
        }

        try {
            s3Service.deleteImagesCdn(removedImages);
        } catch (Exception e) {
            log.warn("공지사항 수정 완료되었으나 S3 이미지 삭제 실패, 고아 파일이 남을 수 있습니다.", e);
            log.warn("삭제 실패한 이미지 리스트: {}", removedImages);
        }
    }

    // 공지사항 등록, 수정 작업 실패시 업로드 되어진 이미지를 삭제
    private void rollbackUploadedImages(List<String> uploadedImageLinks, String operation) {
        if (!uploadedImageLinks.isEmpty()) {
            log.debug("{} 실패, 업로드된 이미지 삭제 진행", operation);
            try {
                s3Service.deleteImagesCdn(uploadedImageLinks);
            } catch (Exception e) {
                log.warn("{} 롤백 중 S3 이미지 삭제 실패. 고아 이미지가 남을 수 있습니다. 실패 목록: {}", operation,
                    uploadedImageLinks, e);
            }
        }
    }


}
