package com.bbangle.bbangle.notification.admin.service.model;

import java.util.List;
import lombok.Builder;

public class AdminNoticeCommand {

    @Builder
    public record AdminNoticeCreateCommand(Long adminId,
                                           String title,
                                           String content,
                                           List<String> cdnImageLinks) {
    }



}
