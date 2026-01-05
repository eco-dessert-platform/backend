package com.bbangle.bbangle.notification.admin.service;


import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.admin.repository.AdminRepository;
import com.bbangle.bbangle.common.service.HtmlContentProcessor;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeCreateCommand;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import com.bbangle.bbangle.notification.domain.Notice;
import com.bbangle.bbangle.notification.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final NotificationRepository notificationRepository;
    private final AdminRepository adminRepository;
    private final ObjectMapper objectMapper;
    private final HtmlContentProcessor htmlContentProcessor;

    @Transactional
    public NoticeInfo createAdminNotification(AdminNoticeCreateCommand command) {
        Admin admin = adminRepository.findById(command.adminId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ADMIN_NOT_FOUND));

        try {

            String imageLinksJson = objectMapper.writeValueAsString(command.cdnImageLinks());

            Notice notice = Notice.createNoticeForAdmin(command.title(), command.content(), imageLinksJson, admin);
            Notice savedNotice = notificationRepository.save(notice);

            return NoticeInfo.from(savedNotice, objectMapper);
        } catch (JsonProcessingException e) {
            throw new BbangleException(BbangleErrorCode.JSON_SERIALIZATION_ERROR);
        }
    }


}
