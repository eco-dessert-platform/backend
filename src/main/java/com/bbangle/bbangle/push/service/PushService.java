package com.bbangle.bbangle.push.service;

import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.domain.PushType;
import com.bbangle.bbangle.push.dto.CreatePushRequest;
import com.bbangle.bbangle.push.dto.FcmPush;
import com.bbangle.bbangle.push.dto.FcmRequest;
import com.bbangle.bbangle.push.dto.PushRequest;
import com.bbangle.bbangle.push.dto.PushResponse;
import com.bbangle.bbangle.push.repository.PushRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

        if (Objects.isNull(push)) {
            Push newPush = Push.builder()
                    .fcmToken(request.fcmToken())
                    .memberId(memberId)
                    .productId(request.productId())
                    .pushType(PushType.valueOf(request.pushType()))
                    .days(request.days())
                    .pushCategory(PushCategory.valueOf(request.pushCategory()))
                    .active(true)
                    .build();

            pushRepository.save(newPush);
            return;
        }

        if (request.pushType() != null) {
            push.updateDays(request.days());
        }
        push.updateActive(true);
    }


    @Transactional
    public void cancelPush(PushRequest request, Long memberId) {
        memberRepository.findMemberById(memberId);
        Push push = pushRepository.findPush(request.productId(), request.pushCategory(), memberId);

        if (Objects.isNull(push)) {
            throw new BbangleException(BbangleErrorCode.PUSH_NOT_FOUND);
        }

        push.updateActive(false);
    }


    @Transactional
    public void deletePush(PushRequest request, Long memberId) {
        memberRepository.findMemberById(memberId);
        Push push = pushRepository.findPush(request.productId(), request.pushCategory(), memberId);

        if (Objects.isNull(push)) {
            throw new BbangleException(BbangleErrorCode.PUSH_NOT_FOUND);
        }

        pushRepository.delete(push);
    }


    @Transactional(readOnly = true)
    public List<PushResponse> getPushes(String pushCategory, Long memberId) {
        memberRepository.findMemberById(memberId);
        return pushRepository.findPushList(pushCategory, memberId);
    }

    @Transactional(readOnly = true)
    public List<FcmRequest> getPushesForNotification() {
        return editMessage(getMustSendAllPushes());
    }

    private List<FcmRequest> getMustSendAllPushes() {
        // 1. 신청된 모든 푸시와 그 상품 Id를 조회한다.
        List<FcmPush> subscribedPushList = pushRepository.findPushList();
        List<Long> subscribedProductIdList = subscribedPushList.stream()
                .map(FcmPush::productId)
                .toList();

        // 2. 전체 상품 중에서 푸시 알림 신청되었으면서 이제 막 입고된 상품 Id를 찾는다.
        Set<Long> targetProductIdSet = productRepository.findProductJustStocked(subscribedProductIdList);

        // 3. 알림이 나가야 할 모든 요청을 조회한다.
        return subscribedPushList.stream()
                .filter(fcmPush -> shouldSendPush(fcmPush, targetProductIdSet))
                .map(FcmRequest::new)
                .toList();
    }


    public List<FcmRequest> editMessage(List<FcmRequest> requestList) {
        // 4. 알림 제목과 내용을 편집한다.
        for (FcmRequest request : requestList) {
            String title = "";
            String body = "";
            if(!request.isSoldOut()){
                title = String.format("%s %s님이 기다리던 상품이 %s되었어요!", "\u23F0", request.getMemberName(), request.getPushCategory());
                body = String.format("[%s] '%s' %n 곧 품절될 수 있으니 지금 확인해보세요.", request.getBoardTitle(), request.getProductTitle());
            } else if (isAfterSoldOutDay(request.getDays())){ //TODO 품절되고 날짜별 판별해 줘야함
                title = String.format("%s 상품이 빠르게 품절 되었어요!", request.getProductTitle());
                body = "주문 가능한 다른 요일에 알림 신청을 해주세요";
            }
            request.editPushMessage(title, body);
        }
        return requestList;
    }

    //사용자가 푸시 알림 신청한 요일 이전에 품절이 되었는지 판단
    private boolean isAfterSoldOutDay(String days) {
        /**
         * TO 순원님
         * 이 쪽을 구현해주시면 됩니다. 물론 꼭 이 위치 아니고 관련 코드 다 변경하셔도 됩니다
         *
         * */
        return true;
    }


    private boolean shouldSendPush(FcmPush fcmPush, Set<Long> targetProductIdSet) {
        if (fcmPush.pushType() == PushType.DATE) {
            return targetProductIdSet.contains(fcmPush.productId());
        } else if (fcmPush.pushType() == PushType.WEEK) {
            return targetProductIdSet.contains(fcmPush.productId()) && doDaysContainToday(fcmPush.days());
        }
        return false;
    }


    private boolean doDaysContainToday(String days) {
        String today = LocalDate.now().getDayOfWeek().toString();
        return Arrays.asList(days.split(", ")).contains(today);
    }

}
