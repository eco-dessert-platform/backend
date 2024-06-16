package com.bbangle.bbangle.notification.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.NOTIFICATION_NOT_FOUND;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.notification.domain.Notice;
import com.bbangle.bbangle.notification.dto.NotificationDetailResponseDto;
import com.bbangle.bbangle.notification.dto.NotificationResponse;
import com.bbangle.bbangle.notification.dto.NotificationUploadRequest;
import com.bbangle.bbangle.notification.repository.NotificationRepository;
import com.bbangle.bbangle.page.NotificationCustomPage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;


    @Transactional(readOnly = true)
    public NotificationCustomPage<List<NotificationResponse>> getList(Long cursorId) {
        return notificationRepository.findNextCursorPage(cursorId);
    }

    public NotificationDetailResponseDto getNoticeDetail(Long id) {
        Notice notification = notificationRepository.findById(id)
            .orElseThrow(() -> new BbangleException(NOTIFICATION_NOT_FOUND));
        return NotificationDetailResponseDto.from(notification);
    }

    @Transactional
    public void upload(NotificationUploadRequest notificationUploadRequest) {
        Notice newNotice = Notice.builder()
            .title(notificationUploadRequest.title())
            .content(notificationUploadRequest.content())
            .build();

        notificationRepository.save(newNotice);
    }

}
