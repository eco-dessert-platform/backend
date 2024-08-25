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
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
                    .pushType(request.pushType())
                    .days(!StringUtils.isBlank(request.days()) ? request.days() : null)
                    .pushCategory(request.pushCategory())
                    .isActive(true)
                    .build();
            pushRepository.save(newPush);
            return;
        }
        push.updateDays(request.days());
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
    public List<PushResponse> getPushes(PushCategory pushCategory, Long memberId) {
        memberRepository.findMemberById(memberId);
        return pushRepository.findPushList(pushCategory, memberId);
    }

    @Transactional(readOnly = true)
    public List<FcmRequest> getPushesForNotification() {
        return editMessage(getMustSendAllPushes());
    }

    private List<FcmRequest> getMustSendAllPushes() {
        List<FcmPush> activatedPushes = pushRepository.findPushList();
        List<Long> subscribedProductIds = activatedPushes.stream()
                .map(FcmPush::productId)
                .toList();

        List<Long> targetProductIds = productRepository.findProductsByActivatedProductIds(subscribedProductIds);

        return activatedPushes.stream()
                .filter(fcmPush -> shouldSendPush(fcmPush, targetProductIds))
                .map(FcmRequest::new)
                .toList();
    }


    public List<FcmRequest> editMessage(List<FcmRequest> requestList) {
        for (FcmRequest request : requestList) {
            String title = "";
            String body = "";
            if(!request.isSoldOut()){
                title = String.format("%s님이 기다리던 상품이 %s되었어요! %s", request.getMemberName(), request.getPushCategory(), "\uD83D\uDE4C");
                body = String.format("[%s] '%s' %n 곧 품절될 수 있으니 지금 확인해보세요.", request.getBoardTitle(), request.getProductTitle());
            } else if (request.getPushType().equals(PushType.WEEK.getDescription())){
                title = String.format("%s %s 상품이 빠르게 품절 되었어요!", "\uD83D\uDEAB", request.getProductTitle());
                body = "주문 가능한 다른 요일에 알림 신청을 해주세요";
            }
            request.editPushMessage(title, body);
        }
        return requestList;
    }

    private boolean shouldSendPush(FcmPush fcmPush, List<Long> targetProductIds) {
        if (fcmPush.pushType() == PushType.DATE) {
            return targetProductIds.contains(fcmPush.productId()) && isEqualToday(fcmPush.date().toLocalDate());
        } else if (fcmPush.pushType() == PushType.WEEK) {
            return targetProductIds.contains(fcmPush.productId()) && doDaysContainToday(fcmPush.days());
        }
        return false;
    }

    private boolean isEqualToday(LocalDate pushDate) {
        return LocalDate.now().isEqual(pushDate);
    }

    private boolean doDaysContainToday(String days) {
        String today = LocalDate.now().getDayOfWeek().toString();
        return Arrays.asList(days.split(", ")).contains(today);
    }

}
