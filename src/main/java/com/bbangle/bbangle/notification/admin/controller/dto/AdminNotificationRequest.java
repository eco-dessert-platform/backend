package com.bbangle.bbangle.notification.admin.controller.dto;

import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeCreateCommand;
import com.bbangle.bbangle.notification.admin.service.model.AdminNoticeCommand.AdminNoticeUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;


public class AdminNotificationRequest {

    @Schema(description = "관리자 공지사항 생성 DTO")
    public record AdminNotificationCreateRequest(
        @Schema(description = "공지사항 제목")
        @NotBlank(message = "공지사항 제목은 필수입니다.")
        String title,
        @Schema(description = "공지사항 본문")
        @NotBlank(message = "공지사항 본문은 필수입니다.")
        String content
    ) {

        public AdminNoticeCreateCommand toCreateCommand(Long adminId, String convertContext ,List<String> imageLinks) {
            return AdminNoticeCreateCommand.builder()
                .adminId(adminId)
                .title(title)
                .content(convertContext)
                .cdnImageLinks(imageLinks)
                .build();
        }

        public AdminNoticeCreateCommand toCreateCommand(Long adminId, String convertContext) {
            return AdminNoticeCreateCommand.builder()
                .adminId(adminId)
                .title(title)
                .content(convertContext)
                .build();
        }
    }


    @Schema(description = "관리자 공지사항 수정 DTO")
    public record AdminNotificationUpdateRequest(
        @Schema(description = "공지사항 제목")
        @NotBlank(message = "공지사항 제목은 필수입니다.")
        String title,
        @Schema(description = "공지사항 본문")
        @NotBlank(message = "공지사항 본문은 필수입니다.")
        String content
    ) {

        public AdminNoticeUpdateCommand toUpdateImageCommand(Long adminId, Long noticeId,String title, String convertContext , List<String> imageLinks) {
            return AdminNoticeUpdateCommand.builder()
                .adminId(adminId)
                .title(title)
                .noticeId(noticeId)
                .content(convertContext)
                .cdnImageLinks(imageLinks)
                .build();
        }


        public AdminNoticeUpdateCommand toUpdateCommand(Long adminId, Long noticeId,String title ,String convertContext) {
            return AdminNoticeUpdateCommand.builder()
                .adminId(adminId)
                .title(title)
                .noticeId(noticeId)
                .content(convertContext)
                .build();
        }

    }




}


