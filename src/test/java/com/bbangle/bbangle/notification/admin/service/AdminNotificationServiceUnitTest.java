package com.bbangle.bbangle.notification.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.admin.repository.AdminRepository;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import com.bbangle.bbangle.notification.domain.Notice;
import com.bbangle.bbangle.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
@DisplayName("[유닛 테스트] AdminNotificationService")
@ExtendWith(MockitoExtension.class)
class AdminNotificationServiceUnitTest {

    @InjectMocks
    private AdminNotificationService adminNotificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("공지사항 조회에 성공한다")
    void success_searchNotice() {
        // given
        Long adminId = 1L;
        Long noticeId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Admin admin = Admin.builder()
            .accountId("test")
            .password("pass")
            .name("name")
            .build();
        ReflectionTestUtils.setField(admin, "id", adminId);

        Notice notice = Notice.builder()
            .id(noticeId)
            .title("title")
            .content("content")
            .build();
        Page<Notice> noticePage = new PageImpl<>(List.of(notice));

        given(notificationRepository.findByIsDeletedFalse(any(Pageable.class))).willReturn(noticePage);

        // when
        Page<NoticeInfo> result = adminNotificationService.searchNotice(pageable);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(noticeId);
        assertThat(result.getContent().get(0).title()).isEqualTo("title");
    }


}