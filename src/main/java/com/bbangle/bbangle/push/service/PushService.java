package com.bbangle.bbangle.push.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.dto.PushRequest;
import com.bbangle.bbangle.push.dto.CreatePushRequest;
import com.bbangle.bbangle.push.dto.PushResponse;
import com.bbangle.bbangle.push.repository.PushRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PushService {

    private final PushRepository pushRepository;
    private final MemberRepository memberRepository;


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
    public List<PushResponse> getPush(String pushCategory, Long memberId) {
        memberRepository.findMemberById(memberId);
        return pushRepository.findPushList(pushCategory, memberId);
    }

}
