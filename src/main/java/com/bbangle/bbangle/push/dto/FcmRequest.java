package com.bbangle.bbangle.push.dto;

import com.bbangle.bbangle.push.domain.PushCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
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

    public FcmRequest(String fcmToken, String memberName, String boardTitle, String productTitle, PushCategory pushCategory) {
        this.fcmToken = fcmToken;
        this.memberName = memberName;
        this.boardTitle = boardTitle;
        this.productTitle = productTitle;
        this.pushCategory = pushCategory.getDescription();
    }

    public void editPushMessage(String title, String body) {
        this.title = title;
        this.body = body;
    }
}
