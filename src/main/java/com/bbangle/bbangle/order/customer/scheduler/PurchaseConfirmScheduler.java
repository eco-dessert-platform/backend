package com.bbangle.bbangle.order.customer.scheduler;

import com.bbangle.bbangle.order.customer.service.PurchaseConfirmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 배송완료 후 7일이 지난 주문상품을 자동으로 구매확정하는 스케줄러.
 * 매일 자정에 실행됩니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseConfirmScheduler {

    private final PurchaseConfirmService purchaseConfirmService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 밤 12시
    public void autoConfirm() {
        log.info("자동 구매확정 스케줄러 시작");
        int confirmed = purchaseConfirmService.autoConfirmExpired();
        log.info("자동 구매확정 스케줄러 종료 - {}건 처리", confirmed);
    }
}
