package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.notification.domain.Notice;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NoticeFixture {

    public static Notice notice(String title, String content, LocalDateTime createdAt) {
        return Notice.builder()
                .title(title)
                .content(content)
                .createdAt(createdAt)
                .build();
    }

}