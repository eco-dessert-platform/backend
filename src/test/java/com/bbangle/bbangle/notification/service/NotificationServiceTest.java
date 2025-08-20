package com.bbangle.bbangle.notification.service;


import static com.bbangle.bbangle.exception.BbangleErrorCode.NOTIFICATION_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.common.page.NotificationCustomPage;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.notification.domain.Notice;
import com.bbangle.bbangle.notification.dto.NotificationDetailResponseDto;
import com.bbangle.bbangle.notification.dto.NotificationResponse;
import com.bbangle.bbangle.notification.dto.NotificationUploadRequest;
import com.bbangle.bbangle.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[비즈니스 로직] NotificationService")
@ExtendWith(MockitoExtension.class) // Mockito 기능 활성화
class NotificationServiceTest {

    @InjectMocks // @Spy, @Mock 등으로 생성된 객체를 주입받아 테스트 대상 객체를 생성
    private NotificationService sut;

    @Mock // Mock 객체 생성
    private NotificationRepository notificationRepository;

    @DisplayName("cursorId를 기준으로 Notification 페이지를 반환한다.")
    @Test
    void givenCursorId_whenGetList_thenReturnNotificationCustomPage() {
        // Given
        Long curorId = 1L;
        Long pageSize = 2L;
        NotificationResponse notificationResponse = new NotificationResponse(
                1L,
                "title1",
                "content1",
                "2023-10-01 12:00"
        );
        NotificationCustomPage<List<NotificationResponse>> expectedPage =
                NotificationCustomPage.from(List.of(notificationResponse), pageSize);
        given(notificationRepository.findNextCursorPage(curorId)).willReturn(expectedPage);

        // When
        NotificationCustomPage<List<NotificationResponse>> result = sut.getList(curorId);

        // Then
        then(notificationRepository).should(times(1)).findNextCursorPage(curorId);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedPage);
    }

    @DisplayName("ID를 제공하면 Notification 상세 정보를 반환한다.")
    @Test
    void givenId_whenGetNoticeDetail_thenReturnNotificationDetail() {
        // Given
        Long id = 1L;
        Notice notice = Notice.builder()
                .id(id)
                .title("title1")
                .content("content1")
                .createdAt(LocalDateTime.of(2023, 11, 12, 12, 0, 0))
                .build();
        given(notificationRepository.findById(id)).willReturn(Optional.of(notice));

        // When
        NotificationDetailResponseDto result = sut.getNoticeDetail(id);

        // Then
        then(notificationRepository).should(times(1)).findById(id);
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(notice.getId());
        assertThat(result.title()).isEqualTo(notice.getTitle());
        assertThat(result.content()).isEqualTo(notice.getContent());
        assertThat(result.createdAt()).isEqualTo(
                notice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
    }

    @DisplayName("존재하지 않는 ID를 제공하면 예외가 발생한다.")
    @Test
    void givenNonExistingId1_whenGetNoticeDetail_thenThrowsException() {
        // Given
        Long nonExistingId = -1L;
        given(notificationRepository.findById(nonExistingId)).willReturn(Optional.empty());

        // When
        BbangleException result = assertThrows(BbangleException.class, () -> sut.getNoticeDetail(nonExistingId));

        // Then
        then(notificationRepository).should(times(1)).findById(nonExistingId);
        assertThat(result).isInstanceOf(BbangleException.class);
        assertThat(result.getBbangleErrorCode()).isEqualTo(NOTIFICATION_NOT_FOUND);
        assertThat(result.getMessage()).isEqualTo(NOTIFICATION_NOT_FOUND.getMessage());
    }

    @DisplayName("Notice 업로드 요청이 주어지면, DB에 저장한다.")
    @Test
    void givenNotificationUploadRequest_whenUpload_thenSaveNotice() {
        // Given
        NotificationUploadRequest request = new NotificationUploadRequest("title1", "content1");
        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class); // 메서드 인자를 검증하기 위해 캡처 사용

        // When
        sut.upload(request);

        // Then
        then(notificationRepository).should(times(1)).save(captor.capture());
        Notice savedNotice = captor.getValue();
        assertThat(savedNotice.getTitle()).isEqualTo(request.title());
        assertThat(savedNotice.getContent()).isEqualTo(request.content());
    }

}