package com.bbangle.bbangle.push.repository;

import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.dto.PushResponse;

import java.util.List;

public interface PushQueryDSLRepository {
    Push findPush(Long boardId, String pushCategory, Long memberId);
    List<PushResponse> findPushList(Long boardId, String pushCategory, Long memberId);
}
