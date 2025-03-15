package com.bbangle.bbangle.notification.repository;

import com.bbangle.bbangle.notification.dto.NotificationResponse;
import com.bbangle.bbangle.common.page.NotificationCustomPage;
import java.util.List;

public interface NotificationQueryDSLRepository {
    NotificationCustomPage<List<NotificationResponse>> findNextCursorPage(Long cursorId);
}
