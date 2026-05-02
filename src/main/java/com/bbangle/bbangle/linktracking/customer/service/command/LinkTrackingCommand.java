package com.bbangle.bbangle.linktracking.customer.service.command;

import com.bbangle.bbangle.linktracking.domain.LinkChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LinkTrackingCommand {

    public record Visit(

        @NotNull(message = "channel은 필수입니다.")
        LinkChannel channel,

        @NotBlank(message = "ipAddress는 필수입니다.")
        String ipAddress,

        String userAgent,

        String referer
    ) {}
}
