package com.bbangle.bbangle.push.scheduler;

import com.bbangle.bbangle.push.dto.FcmRequest;
import com.bbangle.bbangle.push.service.FcmService;
import com.bbangle.bbangle.push.service.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class PushScheduler {

    private final FcmService fcmService;
    private final PushService pushService;

    @Scheduled(cron = "5 0 12 * * ?")
    public void sendPush() {
        List<FcmRequest> requestList = pushService.getPushesForNotification();
        //fcmService.send(requestList);
    }

}
