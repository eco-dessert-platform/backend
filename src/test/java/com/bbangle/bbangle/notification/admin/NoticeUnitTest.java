package com.bbangle.bbangle.notification.admin;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.notification.domain.Notice;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] Notice")
public class NoticeUnitTest {

    private static final Admin TEST_ADMIN = Admin.builder()
        .accountId("test-admin")
        .password("test-password")
        .name("테스트 관리자")
        .build();

    @Test
    @DisplayName("관리자용 공지사항 생성에 성공한다.")
    void success_create_notice_for_admin() {
        // arrange
        String title = "공지사항 제목";
        String content = "공지사항 내용";
        List<String> imageLinks = List.of("https://example.com");

        // act
        Notice notice = Notice.createNoticeForAdmin(title, content, imageLinks, TEST_ADMIN);

        // assert
        assertThat(notice).isNotNull();
        assertThat(notice.getTitle()).isEqualTo(title);
        assertThat(notice.getContent()).isEqualTo(content);
        assertThat(notice.getImageLinks()).isEqualTo(imageLinks);
    }

    @Test
    @DisplayName("title이 null이면 공지사항 생성에 실패한다.")
    void fail_create_notice_when_title_is_null() {
        // arrange
        String title = null;
        String content = "공지사항 내용";
        List<String> imageLinks = List.of("https://example.com");

        // act & assert
        assertThatThrownBy(() -> Notice.createNoticeForAdmin(title, content, imageLinks, TEST_ADMIN))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.TITLE_IS_EMPTY.getMessage());
    }

    @Test
    @DisplayName("title이 empty이면 공지사항 생성에 실패한다.")
    void fail_create_notice_when_title_is_empty() {
        // arrange
        String title = "";
        String content = "공지사항 내용";
        List<String> imageLinks = List.of("https://image.example.com");

        // act & assert
        assertThatThrownBy(() -> Notice.createNoticeForAdmin(title, content, imageLinks, TEST_ADMIN))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.TITLE_IS_EMPTY.getMessage());
    }

    @Test
    @DisplayName("content가 null이면 공지사항 생성에 실패한다.")
    void fail_create_notice_when_content_is_null() {
        // arrange
        String title = "공지사항 제목";
        String content = null;
        List<String> imageLinks = List.of("https://image.example.com");

        // act & assert
        assertThatThrownBy(() -> Notice.createNoticeForAdmin(title, content, imageLinks, TEST_ADMIN))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.CONTENT_IS_EMPTY.getMessage());
    }

    @Test
    @DisplayName("content가 empty이면 공지사항 생성에 실패한다.")
    void fail_create_notice_when_content_is_empty() {
        // arrange
        String title = "공지사항 제목";
        String content = "";
        List<String> imageLinks = List.of("https://image.example.com");

        // act & assert
        assertThatThrownBy(() -> Notice.createNoticeForAdmin(title, content, imageLinks, TEST_ADMIN))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.CONTENT_IS_EMPTY.getMessage());
    }

    @Test
    @DisplayName("imageLinks가 null이어도 공지사항 생성에 성공한다.")
    void success_create_notice_when_imageLinks_is_null() {
        // arrange
        String title = "공지사항 제목";
        String content = "공지사항 내용";
        List<String> imageLinks = null;

        // act
        Notice notice = Notice.createNoticeForAdmin(title, content, imageLinks, TEST_ADMIN);

        // assert
        assertThat(notice).isNotNull();
        assertThat(notice.getTitle()).isEqualTo(title);
        assertThat(notice.getContent()).isEqualTo(content);
        assertThat(notice.getImageLinks()).isNull();
    }

    @Test
    @DisplayName("admin이 null이면 공지사항 생성에 실패한다.")
    void fail_create_notice_when_admin_is_null() {
        // arrange
        String title = "공지사항 제목";
        String content = "공지사항 내용";
        List<String> imageLinks = List.of("https://image.example.com");
        Admin admin = null;

        // act & assert
        assertThatThrownBy(() -> Notice.createNoticeForAdmin(title, content, imageLinks, admin))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.ADMIN_NOT_FOUND.getMessage());
    }
}
