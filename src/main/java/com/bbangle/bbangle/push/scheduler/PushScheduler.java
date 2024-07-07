package com.bbangle.bbangle.push.scheduler;

import com.bbangle.bbangle.push.dto.FcmRequest;
import com.bbangle.bbangle.push.service.FcmService;
import com.bbangle.bbangle.push.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class PushScheduler {

    private final FcmService fcmService;
    private final PushService pushService;


    public void sendPush() {
        List<FcmRequest> requestList = pushService.getPushesForNotification();
        pushService.editMessage(requestList);
        fcmService.send(requestList);
    }

}
