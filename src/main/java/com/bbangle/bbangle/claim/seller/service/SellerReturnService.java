package com.bbangle.bbangle.claim.seller.service;

import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.repository.ReturnRequestRepository;
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
public class SellerReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderItemHistoryRepository orderItemHistoryRepository;

    @Transactional
    public void decision(List<Long> returnIds, Long sellerId, DecisionType decisionType, String reason) {
        Long validatedCount = returnRequestRepository.countReturnsBySeller(returnIds, sellerId);
        if (validatedCount == null || validatedCount != returnIds.size()) {
            throw new BbangleException(BbangleErrorCode.SELLER_CLAIM_MISMATCH);
        }

        List<ReturnRequest> returnRequests = returnRequestRepository.findAllById(returnIds);
        for (ReturnRequest returnRequest : returnRequests) {
            processDecision(returnRequest, decisionType, reason);
        }
    }

    private void processDecision(ReturnRequest returnRequest, DecisionType decisionType, String reason) {
        switch (decisionType) {
            case APPROVE -> {
                returnRequest.approve(reason);
                OrderItem orderItem = returnRequest.getOrderItem();
                orderItem.returnApprove();
                OrderItemHistory history = OrderItemHistory.create(orderItem);
                orderItemHistoryRepository.save(history);
            }
            case REJECT -> {
                returnRequest.reject(reason);
                OrderItem orderItem = returnRequest.getOrderItem();
                orderItem.returnReject();
                OrderItemHistory history = OrderItemHistory.create(orderItem);
                orderItemHistoryRepository.save(history);
            }
        }
    }

}
