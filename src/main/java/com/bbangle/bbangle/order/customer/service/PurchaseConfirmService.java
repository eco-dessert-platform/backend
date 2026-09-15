package com.bbangle.bbangle.order.customer.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.OrderDelivery;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.repository.OrderDeliveryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구매확정 서비스.
 *
 * <p>정책: 배송완료(DELIVERED) 후 7일이 지나면 자동으로 구매확정되며, 그 전이라도 고객이 직접 구매확정할 수 있습니다.
 * 구매확정 이후에는 화면 버튼이 "후기작성"으로 전환됩니다(노출 여부는 주문 조회 응답의 플래그로 내려줍니다).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseConfirmService {

    /** 배송완료 후 자동 구매확정까지의 기간(일). */
    public static final int AUTO_CONFIRM_DEADLINE_DAYS = 7;

    private final OrderItemRepository orderItemRepository;
    private final OrderDeliveryRepository orderDeliveryRepository;

    /**
     * 고객의 수동 구매확정.
     * 본인 소유의 배송완료된 주문상품만 구매확정할 수 있습니다.
     *
     * @return 구매확정된 주문상품 ID
     */
    @Transactional
    public Long confirm(Long memberId, Long orderId, Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findOwnedOrderItem(memberId, orderId, orderItemId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.CUSTOMER_ORDER_ITEM_NOT_FOUND));

        if (!isDelivered(orderItemId)) {
            throw new BbangleException(BbangleErrorCode.PURCHASE_CONFIRM_NOT_ALLOWED);
        }

        orderItem.confirmPurchase(LocalDateTime.now());
        return orderItem.getId();
    }

    /**
     * 배송완료 후 7일이 지난 주문상품을 일괄 자동 구매확정합니다. (스케줄러에서 호출)
     */
    @Transactional
    public int autoConfirmExpired() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(AUTO_CONFIRM_DEADLINE_DAYS);
        List<OrderItem> targets = orderItemRepository.findAutoConfirmTargets(cutoff);

        LocalDateTime confirmedAt = LocalDateTime.now();
        targets.forEach(orderItem -> orderItem.confirmPurchase(confirmedAt));

        log.info("배송완료 후 {}일 경과 자동 구매확정 완료 - {}건 (cutoff={})",
            AUTO_CONFIRM_DEADLINE_DAYS, targets.size(), cutoff);
        return targets.size();
    }

    private boolean isDelivered(Long orderItemId) {
        return orderDeliveryRepository.findByOrderItemId(orderItemId)
            .map(OrderDelivery::getStatus)
            .filter(status -> status == OrderDeliveryStatus.DELIVERED)
            .isPresent();
    }
}
