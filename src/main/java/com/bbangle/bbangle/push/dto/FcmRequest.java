package com.bbangle.bbangle.push.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FcmRequest {
    private String fcmToken;
    private String memberName;
    private String boardTitle;
    private String productTitle;
    private String pushCategory;
    private String title;
    private String body;

    public FcmRequest(String fcmToken, String memberName, String boardTitle, String productTitle, String pushCategory) {
        this.fcmToken = fcmToken;
        this.memberName = memberName;
        this.boardTitle = boardTitle;
        this.productTitle = productTitle;
        this.pushCategory = pushCategory;
    }

    public void setPushMessage(String title, String body) {
        this.title = title;
        this.body = body;
    }
}
