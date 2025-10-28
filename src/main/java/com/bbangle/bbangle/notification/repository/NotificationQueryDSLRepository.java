package com.bbangle.bbangle.notification.repository;

import com.bbangle.bbangle.common.page.NotificationCustomPage;
import com.bbangle.bbangle.notification.customer.dto.NotificationResponse;
import java.util.List;

public interface NotificationQueryDSLRepository {

    NotificationCustomPage<List<NotificationResponse>> findNextCursorPage(Long cursorId);
}
