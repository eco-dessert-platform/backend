package com.bbangle.bbangle.push.customer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class FcmRequest {
    private Long pushId;
    private String fcmToken;
    private String memberName;
    private String boardTitle;
    private String productTitle;
    private String pushType;
    private String days;
    private String pushCategory;
    private String title;
    private String body;
    private boolean isSoldOut;

    public FcmRequest(FcmPush fcmPush) {
        this.pushId = fcmPush.pushId();
        this.fcmToken = fcmPush.fcmToken();
        this.memberName = fcmPush.memberName();
        this.boardTitle = fcmPush.boardTitle();
        this.productTitle = fcmPush.productTitle();
        this.pushType = fcmPush.pushType() != null ? fcmPush.pushType().getDescription() : null;
        this.days = fcmPush.days();
        this.pushCategory = fcmPush.pushCategory().getDescription();
        this.isSoldOut = fcmPush.isSoldOut();
    }

    public void editPushMessage(String title, String body) {
        this.title = title;
        this.body = body;
    }
}
