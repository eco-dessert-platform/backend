package com.bbangle.bbangle.notification.admin.service;


import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.admin.repository.AdminRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeCreateCommand;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeUpdateCommand;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationDeleteResponse;
import com.bbangle.bbangle.notification.admin.controller.dto.AdminNotificationResponse.AdminNotificationDeleteResponse.FailedNotice;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import com.bbangle.bbangle.notification.domain.Notice;
import com.bbangle.bbangle.notification.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationService {

    private final NotificationRepository notificationRepository;
    private final AdminRepository adminRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public NoticeInfo createAdminNotification(AdminNoticeCreateCommand command) {
        Admin admin = adminRepository.findById(command.adminId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ADMIN_NOT_FOUND));

        try {

            if (command.cdnImageLinks() == null) {
                Notice savedNotice = notificationRepository.save(
                    Notice.createNoticeFofAdminWithoutImage(command.title(), command.content(), admin));

                return NoticeInfo.from(savedNotice);
            }
            String imageLinksJson = objectMapper.writeValueAsString(command.cdnImageLinks());

            Notice notice = Notice.createNoticeForAdmin(command.title(), command.content(), imageLinksJson, admin);
            Notice savedNotice = notificationRepository.save(notice);

            return NoticeInfo.from(savedNotice, objectMapper);
        } catch (JsonProcessingException e) {
            throw new BbangleException(BbangleErrorCode.JSON_SERIALIZATION_ERROR);
        }
    }


    @Transactional
    public NoticeInfo updateAdminNotification(AdminNoticeUpdateCommand command) {
        Admin admin = adminRepository.findById(command.adminId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ADMIN_NOT_FOUND));

        try {
            Notice beforeNotice = notificationRepository.findById(command.noticeId())
                .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOT_FIND_NOTICE));

            if (command.cdnImageLinks() != null) {

                String imageLinksJson = objectMapper.writeValueAsString(command.cdnImageLinks());
                beforeNotice.updateNoticeContainImage(command.title(), command.content(), imageLinksJson);
                return NoticeInfo.from(beforeNotice, objectMapper);
            }

            beforeNotice.updateNoticeWithoutImage(command.title(), command.content());

            return NoticeInfo.from(beforeNotice);

        } catch (JsonProcessingException e) {
            throw new BbangleException(BbangleErrorCode.JSON_SERIALIZATION_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getExistingImageLinks(Long noticeId) {
        Notice notice = notificationRepository.findById(noticeId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOT_FIND_NOTICE));

        return deserializeImageLinks(notice.getImageLinks());
    }

    private List<String> deserializeImageLinks(String imageLinksJson) {
        if (imageLinksJson == null) {
            return List.of();
        }

        try {
            return objectMapper.readValue(imageLinksJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new BbangleException(BbangleErrorCode.JSON_SERIALIZATION_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public NoticeInfo getNotice(Long noticeId) {
        Notice notice = notificationRepository.findByIdAndIsDeletedFalse(noticeId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOT_FIND_NOTICE));

        return NoticeInfo.from(notice, objectMapper);
    }

    @Transactional(readOnly = true)
    public Page<NoticeInfo> searchNotice(Pageable pageable) {
        Page<Notice> notice = notificationRepository.findByIsDeletedFalse(pageable);
        return notice.map(NoticeInfo::from);
    }

    @Transactional
    public AdminNotificationDeleteResponse deleteNotification(Long adminId, List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
             throw new BbangleException(BbangleErrorCode._BAD_REQUEST);
        }

        List<Long> distinctIds = noticeIds.stream().distinct().toList();
        List<Notice> notices = notificationRepository.findAllByIdInAndAdminId(distinctIds, adminId);

        // 삭제 성공 처리
        notices.forEach(Notice::delete);
        notificationRepository.saveAll(notices);

        // 실패한 공지사항 목록 생성
        List<Long> foundIds = notices.stream().map(Notice::getId).toList();
        List<FailedNotice> failedNotices = distinctIds.stream()
            .filter(id -> !foundIds.contains(id))
            .map(id -> FailedNotice.builder()
                .id(id)
                .title("[존재하지 않는 공지사항]")
                .build())
            .toList();

        int successCount = notices.size();
        return AdminNotificationDeleteResponse.of(successCount, failedNotices);
    }

}