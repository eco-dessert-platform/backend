package com.bbangle.bbangle.push.repository;

import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.customer.dto.FcmPush;
import com.bbangle.bbangle.push.customer.dto.PushResponse;

import java.util.List;

public interface PushQueryDSLRepository {
    Push findPush(Long productId, PushCategory pushCategory, Long memberId);
    List<PushResponse> findPushList(PushCategory pushCategory, Long memberId);
    List<FcmPush> findPushList();
    List<Long> findExistingPushProductIds(List<Long> productIds, Long memberId);
}
