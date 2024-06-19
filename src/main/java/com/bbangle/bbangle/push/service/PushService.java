package com.bbangle.bbangle.push.service;

import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.dto.PushRequest;
import com.bbangle.bbangle.push.dto.CreatePushRequest;
import com.bbangle.bbangle.push.repository.PushRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PushService {

    private final PushRepository pushRepository;
    private final MemberRepository memberRepository;


    public void createPush(CreatePushRequest request, Long memberId) {
        memberRepository.findMemberById(memberId);
        Push newPush = Push.builder()
                .fcmToken(request.fcmToken())
                .memberId(memberId)
                .productId(request.productId())
                .pushCategory(PushCategory.valueOf(request.pushCategory()))
                .pushStatus(false)
                .build();

        pushRepository.save(newPush);
    }


    public void recreatePush(PushRequest request, Long memberId) {
        memberRepository.findMemberById(memberId);
        Push push = pushRepository.findPush(request, memberId);
        push.recreatePush();
    }


    public void cancelPush(PushRequest request, Long memberId) {
        memberRepository.findMemberById(memberId);
        Push push = pushRepository.findPush(request, memberId);
        push.cancelPush();
    }


    public void deletePush(PushRequest request, Long memberId) {
        memberRepository.findMemberById(memberId);
        Push push = pushRepository.findPush(request, memberId);
        pushRepository.delete(push);
    }


    public List<Push> getPush(PushRequest request, Long memberId) {
        memberRepository.findMemberById(memberId);
        pushRepository.findPushList(request, memberId);
        return null;
    }


}
