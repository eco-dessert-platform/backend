package com.bbangle.bbangle.notification.domain;

import com.bbangle.bbangle.admin.domain.Admin;
import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.notification.customer.dto.NotificationResponse;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "notice")
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    @Column(columnDefinition = "JSON")
    private String imageLinks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Admin admin;

    public static NotificationResponse makeNotificationResponse(Notice notice) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDateTime = notice.getCreatedAt().format(formatter);
        return new NotificationResponse(
            notice.getId(),
            notice.getTitle(),
            notice.getContent(),
            formattedDateTime
        );
    }

    public static Notice createNoticeForAdmin(String title, String content, String imageLinks, Admin admin) {
        return new Notice(title, content, imageLinks, admin);
    }

    public static Notice createNoticeFofAdminWithoutImage(String title, String content, Admin admin){
        return new Notice(title, content, admin);
    }


    public void updateNoticeContainImage(String title, String content, String imageLinks){
        this.title = Objects.requireNonNullElse(title, this.title);
        this.content = Objects.requireNonNullElse(content, this.content);
        this.imageLinks = Objects.requireNonNullElse(imageLinks, this.imageLinks);
    }

    public void updateNoticeWithoutImage(String title, String content){
        this.title = Objects.requireNonNullElse(title, this.title);
        this.content = Objects.requireNonNullElse(content, this.content);
    }

    public Notice(String title, String content, String imageLinks, Admin admin) {
        validateField(title, content, admin);
        this.title = title;
        this.content = content;
        this.imageLinks = imageLinks;
        this.admin = admin;
    }

    public Notice(String title, String content,  Admin admin){
        validateField(title, content, admin);
        this.title = title;
        this.content = content;
        this.imageLinks = "[]";
        this.admin = admin;
    }

    private void validateField(String title, String content, Admin admin) {
        if (title == null || title.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.TITLE_IS_EMPTY);
        }
        if (content == null || content.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.CONTENT_IS_EMPTY);
        }
        if (admin == null) {
            throw new BbangleException(BbangleErrorCode.ADMIN_NOT_FOUND);
        }
    }

}
