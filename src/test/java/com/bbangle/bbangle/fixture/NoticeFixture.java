package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.notification.domain.Notice;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NoticeFixture {

    public static Admin createTestAdmin() {
        return Admin.builder()
            .accountId("test-admin")
            .password("test-password")
            .name("테스트 관리자")
            .build();
    }

    public static Notice notice(String title, String content, Admin admin) {
        return new Notice(title, content, null, null, admin);
    }

    public static Notice notice(String title, String content, LocalDateTime createdAt, Admin admin) {
        Notice notice = new Notice(title, content, null, null, admin);
        setCreatedAt(notice, createdAt);
        return notice;
    }

    public static Notice noticeWithIdAndCreatedAt(Long id, String title, String content, LocalDateTime createdAt, Admin admin) {
        Notice notice = new Notice(title, content, null, null, admin);
        setId(notice, id);
        setCreatedAt(notice, createdAt);
        return notice;
    }

    private static void setId(Notice notice, Long id) {
        try {
            Field field = notice.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(notice, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set id field", e);
        }
    }

    private static void setCreatedAt(Notice notice, LocalDateTime createdAt) {
        try {
            Field field = notice.getClass().getSuperclass().getSuperclass().getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(notice, createdAt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set createdAt field", e);
        }
    }

}