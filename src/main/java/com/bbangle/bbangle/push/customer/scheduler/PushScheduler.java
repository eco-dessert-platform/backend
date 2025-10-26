package com.bbangle.bbangle.push.customer.scheduler;

import com.bbangle.bbangle.push.customer.dto.FcmRequest;
import com.bbangle.bbangle.push.customer.service.FcmService;
import com.bbangle.bbangle.push.customer.service.PushService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class PushScheduler {

    private final FcmService fcmService;
    private final PushService pushService;

    //매일 정오
    @Scheduled(cron = "0 0 12 * * *")
    public void sendPush() {
        List<FcmRequest> requestList = pushService.getPushesForNotification();
        fcmService.sendMessage(requestList);
    }
}
