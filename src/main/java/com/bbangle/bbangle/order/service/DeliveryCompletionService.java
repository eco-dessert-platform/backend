package com.bbangle.bbangle.order.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.OrderDelivery;
import com.bbangle.bbangle.order.repository.OrderDeliveryRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 배송완료 처리 서비스.
 *
 * <p>주문상품의 배송정보를 배송완료(DELIVERED)로 전환하고 배송완료 시각({@code deliveredAt})을 기록합니다.
 * 이 시각은 "배송완료 후 7일 자동 구매확정"({@code PurchaseConfirmService}) 배치의 기산점이 됩니다.
 *
 * <p>TODO: 현재는 배송완료를 알려주는 트리거가 연결되어 있지 않습니다.
 * 추후 택배사 배송추적 API(웹훅)를 수신하는 컨트롤러를 추가하고, 운송장 상태가 "배송완료"로 바뀌면
 * 이 {@link #completeDelivery(Long, LocalDateTime)} 를 호출하도록 연결해야 합니다.
 * (연동 전까지는 이 메서드를 호출하는 진입점이 없어 deliveredAt 이 채워지지 않습니다.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryCompletionService {

    private final OrderDeliveryRepository orderDeliveryRepository;

    /**
     * 주문상품의 배송을 완료 처리합니다.
     *
     * @param orderItemId 배송완료된 주문상품 ID
     * @param deliveredAt 택배사가 통지한 실제 배송완료 시각
     */
    @Transactional
    public void completeDelivery(Long orderItemId, LocalDateTime deliveredAt) {
        OrderDelivery delivery = orderDeliveryRepository.findByOrderItemId(orderItemId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.DELIVERY_NOT_FOUND));

        delivery.markDelivered(deliveredAt);
        log.info("배송완료 처리 - orderItemId={}, deliveredAt={}", orderItemId, deliveredAt);
    }
}
