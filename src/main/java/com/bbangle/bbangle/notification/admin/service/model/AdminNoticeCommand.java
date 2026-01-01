package com.bbangle.bbangle.notification.admin.service.model;

import com.bbangle.bbangle.notification.admin.controller.dto.LinkDto;
import java.util.List;
import lombok.Builder;

public class AdminNoticeCommand {

    @Builder
    public record AdminNoticeCreateCommand(Long adminId,
                                           String title,
                                           String content,
                                           List<LinkDto> links,
                                           List<String> imageLinks) {
    }



}
