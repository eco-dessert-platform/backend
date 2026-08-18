package com.bbangle.bbangle.notification.admin.controller.dto;

import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeInfo.NoticeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

public class AdminNotificationResponse {

    @Schema(description = "관리자 공지사항 응답 DTO")
    @Builder
    public record AdminNotificationCreateResponse(
        @Schema(description = "공지사항 ID")
        Long id,
        @Schema(description = "제목")
        String title,
        @Schema(description = "내용")
        String content,
        @Schema(description = "이미지 링크 목록")
        List<String> imageLinks,
        @Schema(description = "생성 일시")
        LocalDateTime createAt,
        @Schema(description = "수정 일시")
        LocalDateTime modifiedAt
    ){

        public static AdminNotificationCreateResponse from(NoticeInfo noticeInfo) {
            return AdminNotificationCreateResponse.builder()
                .id(noticeInfo.id())
                .title(noticeInfo.title())
                .content(noticeInfo.content())
                .imageLinks(noticeInfo.imageLinks())
                .createAt(noticeInfo.createAt())
                .modifiedAt(noticeInfo.modifiedAt())
                .build();
        }
    }

    @Schema(description = "관리자 공지사항 수정 응답 DTO")
    @Builder
    public record AdminNotificationUpdateResponse(
        @Schema(description = "공지사항 ID")
        Long id,
        @Schema(description = "제목")
        String title,
        @Schema(description = "내용")
        String content,
        @Schema(description = "이미지 링크 목록")
        List<String> imageLinks,
        @Schema(description = "생성 일시")
        LocalDateTime createAt,
        @Schema(description = "수정 일시")
        LocalDateTime modifiedAt
    ){

        public static AdminNotificationUpdateResponse from(NoticeInfo noticeInfo) {
            return AdminNotificationUpdateResponse.builder()
                .id(noticeInfo.id())
                .title(noticeInfo.title())
                .content(noticeInfo.content())
                .imageLinks(noticeInfo.imageLinks())
                .createAt(noticeInfo.createAt())
                .modifiedAt(noticeInfo.modifiedAt())
                .build();
        }
    }

    @Schema(description = "(관리자) 공지사항 조회 응답 DTO")
    @Builder
    public record AdminNotificationSearchResponse(
        @Schema(description = "공지사항 ID")
        Long noticeId,
        @Schema(description = "제목")
        String title,
        @Schema(description = "생성 일시")
        LocalDateTime createAt,
        @Schema(description = "수정 일시")
        LocalDateTime modifiedAt
    ){
        public static AdminNotificationSearchResponse from(NoticeInfo noticeInfo) {
            return AdminNotificationSearchResponse.builder()
                .noticeId(noticeInfo.id())
                .title(noticeInfo.title())
                .createAt(noticeInfo.createAt())
                .modifiedAt(noticeInfo.modifiedAt())
                .build();
        }
    }

    @Schema(description = "관리자 공지사항 삭제 응답 DTO")
    @Builder
    public record AdminNotificationDeleteResponse(
        @Schema(description = "삭제 성공한 공지사항 개수")
        int successCount,
        @Schema(description = "삭제 실패한 공지사항 개수")
        int failureCount,
        @Schema(description = "삭제 실패한 공지사항 정보 목록")
        List<FailedNotice> failedNotices
    ){
        @Builder
        public record FailedNotice(
            @Schema(description = "공지사항 ID")
            Long id,
            @Schema(description = "공지사항 제목")
            String title
        ) {}

        public static AdminNotificationDeleteResponse of(int successCount, List<FailedNotice> failedNotices) {
            return AdminNotificationDeleteResponse.builder()
                .successCount(successCount)
                .failureCount(failedNotices.size())
                .failedNotices(failedNotices)
                .build();
        }
    }
}
