package com.bbangle.bbangle.push.repository;

import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.dto.PushResponse;

import java.util.List;

public interface PushQueryDSLRepository {
    Push findPush(Long productId, String pushCategory, Long memberId);
    List<PushResponse> findPushList(String pushCategory, Long memberId);
}
