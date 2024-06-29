package com.bbangle.bbangle.push.scheduler;

import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.push.dto.FcmPush;
import com.bbangle.bbangle.push.dto.FcmRequest;
import com.bbangle.bbangle.push.repository.PushRepository;
import com.bbangle.bbangle.push.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class PushScheduler {

    private final FcmService fcmService;
    private final PushRepository pushRepository;
    private final ProductRepository productRepository;


    public void sendPush() {
        // 1. 신청된 모든 푸시와 그 상품 Id를 조회한다.
        List<FcmPush> subscribedPushList = pushRepository.findPushList();
        List<Long> subscribedProductIdList = subscribedPushList.stream()
                .map(FcmPush::productId)
                .toList();

        // 2. 전체 상품 중에서 푸시 알림 신청되었으면서 이제 막 입고된 상품 Id를 찾는다.
        Set<Long> targetProductIdSet = productRepository.findProductJustStocked(subscribedProductIdList);

        // 3. 알림이 나가야 할 모든 요청을 조회한다.
        List<FcmRequest> requestList = subscribedPushList.stream()
                .filter(fcmPush -> targetProductIdSet.contains(fcmPush.productId()))
                .map(fcmPush -> new FcmRequest(fcmPush.fcmToken(), fcmPush.memberName(), fcmPush.boardTitle(), fcmPush.productTitle(), fcmPush.pushCategory()))
                .toList();

        // 4. 알림 제목과 내용을 편집한다.
        for (FcmRequest request : requestList) {
            String title = String.format("%s님이 기다리던 상품이 %s되었어요!", request.getMemberName(), request.getPushCategory());
            String body = String.format("[%s] '%s' 곧 품절될 수 있으니 지금 확인해보세요.", request.getBoardTitle(), request.getProductTitle());
            request.setPushMessage(title, body);
            System.out.println("title = " + title);
            System.out.println("body = " + body);
        }

        // 5. fcm 서버로 알림을 요청한다.
        fcmService.send(requestList);

    }

}
