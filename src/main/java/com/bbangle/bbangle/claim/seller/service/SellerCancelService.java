package com.bbangle.bbangle.claim.seller.service;

import com.bbangle.bbangle.claim.domain.CancelRequest;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.repository.CancelRequestRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.OrderItemHistory;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SellerCancelService {

    private final CancelRequestRepository cancelRequestRepository;
    private final OrderItemHistoryRepository orderItemHistoryRepository;

    @Transactional
    public void decision(List<Long> cancelIds, Long sellerId, DecisionType decisionType, String reason) {
        Long validatedCount = cancelRequestRepository.countCancelsBySeller(cancelIds, sellerId);
        if (validatedCount == null || validatedCount != cancelIds.size()) {
            throw new BbangleException(BbangleErrorCode.SELLER_CLAIM_MISMATCH);
        }

        List<CancelRequest> cancelRequests = cancelRequestRepository.findAllById(cancelIds);
        for (CancelRequest cancelRequest : cancelRequests) {
            switch (decisionType) {
                case APPROVE -> {
                    cancelRequest.approve(reason);
                    OrderItem orderItem = cancelRequest.getOrderItem();
                    orderItem.cancelApprove();
                    OrderItemHistory history = OrderItemHistory.create(orderItem);
                    orderItemHistoryRepository.save(history);
                }
                case REJECT -> {
                    cancelRequest.reject(reason);
                    OrderItem orderItem = cancelRequest.getOrderItem();
                    orderItem.cancelReject();
                    OrderItemHistory history = OrderItemHistory.create(orderItem);
                    orderItemHistoryRepository.save(history);
                }
            }
        }
    }

}
