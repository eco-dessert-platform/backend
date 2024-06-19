package com.bbangle.bbangle.push.repository;

import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.dto.PushRequest;

import java.util.List;

public interface PushQueryDSLRepository {
    Push findPush(PushRequest request, Long memberId);
    List<Push> findPushList(PushRequest request, Long memberId);
}
