package com.bbangle.bbangle.push.service;

import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.dto.FcmPush;
import com.bbangle.bbangle.push.dto.FcmRequest;
import com.bbangle.bbangle.push.dto.PushRequest;
import com.bbangle.bbangle.push.dto.CreatePushRequest;
import com.bbangle.bbangle.push.dto.PushResponse;
import com.bbangle.bbangle.push.repository.PushRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PushService {

    private final PushRepository pushRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;


    @Transactional
    public void createPush(CreatePushRequest request, Long memberId) {
        memberRepository.findMemberById(memberId);
        Push push = pushRepository.findPush(request.productId(), request.pushCategory(), memberId);

        if (push == null) {
            Push newPush = Push.builder()
                    .fcmToken(request.fcmToken())
                    .memberId(memberId)
                    .productId(request.productId())
                    .pushCategory(PushCategory.valueOf(request.pushCategory()))
                    .subscribed(true)
                    .build();

            pushRepository.save(newPush);
        } else {
            push.updateSubscribed(true);
        }
    }


    @Transactional
    public void cancelPush(PushRequest request, Long memberId) {
        memberRepository.findMemberById(memberId);
        Push push = pushRepository.findPush(request.productId(), request.pushCategory(), memberId);

        if (push == null) {
            throw new BbangleException(BbangleErrorCode.PUSH_NOT_FOUND);
        } else {
            push.updateSubscribed(false);
        }
    }


    @Transactional
    public void deletePush(PushRequest request, Long memberId) {
        memberRepository.findMemberById(memberId);
        Push push = pushRepository.findPush(request.productId(), request.pushCategory(), memberId);

        if (push == null) {
            throw new BbangleException(BbangleErrorCode.PUSH_NOT_FOUND);
        } else {
            pushRepository.delete(push);
        }
    }


    @Transactional(readOnly = true)
    public List<PushResponse> getPushes(String pushCategory, Long memberId) {
        memberRepository.findMemberById(memberId);
        return pushRepository.findPushList(pushCategory, memberId);
    }


    public List<FcmRequest> getPushesForNotification() {
        // 1. 신청된 모든 푸시와 그 상품 Id를 조회한다.
        List<FcmPush> subscribedPushList = pushRepository.findPushList();
        List<Long> subscribedProductIdList = subscribedPushList.stream()
                .map(FcmPush::productId)
                .toList();

        // 2. 전체 상품 중에서 푸시 알림 신청되었으면서 이제 막 입고된 상품 Id를 찾는다.
        Set<Long> targetProductIdSet = productRepository.findProductJustStocked(subscribedProductIdList);

        // 3. 알림이 나가야 할 모든 요청을 조회한다.
        return subscribedPushList.stream()
                .filter(fcmPush -> targetProductIdSet.contains(fcmPush.productId()))
                .map(fcmPush -> new FcmRequest(fcmPush.fcmToken(), fcmPush.memberName(), fcmPush.boardTitle(), fcmPush.productTitle(), fcmPush.pushCategory()))
                .toList();
    }


    public void editMessage(List<FcmRequest> requestList) {
        // 4. 알림 제목과 내용을 편집한다.
        for (FcmRequest request : requestList) {
            String title = String.format("%s님이 기다리던 상품이 %s되었어요!", request.getMemberName(), request.getPushCategory());
            String body = String.format("[%s] '%s' 곧 품절될 수 있으니 지금 확인해보세요.", request.getBoardTitle(), request.getProductTitle());
            request.editPushMessage(title, body);
        }
    }

}
