package com.bbangle.bbangle.notification.admin.service.model;

import com.bbangle.bbangle.notification.domain.Notice;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

public class AdminNoticeInfo {

    @Builder
    public record NoticeInfo(Long id,
                             String title,
                             String content,
                             List<String> imageLinks,
                             LocalDateTime createAt,
                             LocalDateTime modifiedAt) {

        public static NoticeInfo from(Notice notice) {
            return NoticeInfo.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .imageLinks(notice.getImageLinks() != null ? notice.getImageLinks() : List.of())
                .createAt(notice.getCreatedAt())
                .modifiedAt(notice.getModifiedAt())
                .build();
        }

    }



}
