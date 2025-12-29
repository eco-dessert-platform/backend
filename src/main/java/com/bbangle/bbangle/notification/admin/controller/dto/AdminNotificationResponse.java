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
        @Schema(description = "링크 목록")
        List<LinkDto> links,
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
                .links(noticeInfo.links())
                .imageLinks(noticeInfo.imageLinks())
                .createAt(noticeInfo.createAt())
                .modifiedAt(noticeInfo.modifiedAt())
                .build();
        }
    }


}
