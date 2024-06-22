package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PushFixture {


    public static Push newBbangketingPush(Long memberId, Long storeId, Long boardId, Long productId) {
        return Push.builder()
                .fcmToken("testFcmToken1")
                .memberId(memberId)
                .storeId(storeId)
                .boardId(boardId)
                .productId(productId)
                .pushCategory(PushCategory.BBANGCKETING)
                .subscribed(true)
                .build();
    }


    public static Push newRestockPush(Long memberId, Long storeId, Long boardId, Long productId) {
        return Push.builder()
                .fcmToken("testFcmToken1")
                .memberId(memberId)
                .storeId(storeId)
                .boardId(boardId)
                .productId(productId)
                .pushCategory(PushCategory.RESTOCK)
                .subscribed(true)
                .build();
    }


    public static Push newCanceledPush(Long memberId, Long storeId, Long boardId, Long productId) {
        return Push.builder()
                .fcmToken("testFcmToken1")
                .memberId(memberId)
                .storeId(storeId)
                .boardId(boardId)
                .productId(productId)
                .pushCategory(PushCategory.BBANGCKETING)
                .subscribed(false)
                .build();
    }


}
