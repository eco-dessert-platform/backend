package com.bbangle.bbangle.page;

import com.bbangle.bbangle.notification.dto.NotificationResponse;
import java.util.List;

public class NotificationCustomPage<T> extends CustomPage<T> {

    public NotificationCustomPage(T content, Long requestCursor, Boolean hasNext) {
        super(content, requestCursor, hasNext);
    }

    public static NotificationCustomPage<List<NotificationResponse>> from(
        List<NotificationResponse> responseList,
        Long pageSize
    ) {
        boolean hasNext = responseList.size() > pageSize;
        Long requestCursor = !responseList.isEmpty() ? responseList.get(responseList.size() - 1).id() : 0L;

        List<NotificationResponse> limitedResponseList = hasNext
            ? responseList.stream().limit(pageSize).toList()
            : responseList;

        return new NotificationCustomPage<>(limitedResponseList, requestCursor, hasNext);
    }
}
