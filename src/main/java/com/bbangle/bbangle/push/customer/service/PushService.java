package com.bbangle.bbangle.push.customer.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.push.customer.dto.CreatePushRequest;
import com.bbangle.bbangle.push.customer.dto.FcmPush;
import com.bbangle.bbangle.push.customer.dto.FcmRequest;
import com.bbangle.bbangle.push.customer.dto.PushRequest;
import com.bbangle.bbangle.push.customer.dto.PushResponse;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.domain.PushType;
import com.bbangle.bbangle.push.repository.PushRepository;
import io.micrometer.common.util.StringUtils;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushService {

    private final PushRepository pushRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createPush(CreatePushRequest request, Long memberId) {
        memberRepository.findMemberById(memberId);
        Push push = pushRepository.findPush(request.productId(), request.pushCategory(), memberId);
        if (Objects.isNull(push)) {
            Push newPush = Push.builder()
                .fcmToken(request.fcmToken())
                .memberId(memberId)
                .productId(request.productId())
                .pushType(request.pushType() != null ? request.pushType() : null)
                .days(isDaysNull(request) ? null : request.days())
                .pushCategory(request.pushCategory())
                .isActive(true)
                .build();
            pushRepository.save(newPush);
            return;
        }
        if (!isDaysNull(request)) {
            push.updateDays(request.days());
        }
        push.updateActive(true);
    }

    private static boolean isDaysNull(CreatePushRequest request) {
        return StringUtils.isBlank(request.days());
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
        Set<Long> targetProductIds = new HashSet<>(activatedPushes.stream()
            .map(FcmPush::productId)
            .toList());

        return activatedPushes.stream()
            .filter(fcmPush -> shouldSendPush(fcmPush, targetProductIds))
            .map(FcmRequest::new)
            .toList();
    }


    public List<FcmRequest> editMessage(List<FcmRequest> requestList) {
        for (FcmRequest request : requestList) {
            String title = "";
            String body = "";
            if (!request.isSoldOut()) {
                title = String.format("%s님이 기다리던 상품이 %s되었어요! %s", request.getMemberName(),
                    request.getPushCategory(), "\uD83D\uDE4C");
                body = String.format("[%s] '%s' %n 곧 품절될 수 있으니 지금 확인해보세요.", request.getBoardTitle(),
                    request.getProductTitle());
            } else if (request.getPushType().equals(PushType.WEEK.getDescription())) {
                title = String.format("%s %s 상품이 빠르게 품절 되었어요!", "\uD83D\uDEAB",
                    request.getProductTitle());
                body = "주문 가능한 다른 요일에 알림 신청을 해주세요";
            }
            request.editPushMessage(title, body);
        }
        return requestList;
    }

    private boolean shouldSendPush(FcmPush fcmPush, Set<Long> targetProductIds) {
        if (fcmPush.pushCategory() == PushCategory.RESTOCK) {
            return true;
        } else if (fcmPush.pushType() == PushType.DATE) {
            return targetProductIds.contains(fcmPush.productId()) && isEqualToday(
                fcmPush.date().toLocalDate());
        } else if (fcmPush.pushType() == PushType.WEEK) {
            return targetProductIds.contains(fcmPush.productId()) && doDaysContainToday(
                fcmPush.days());
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
