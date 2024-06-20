package com.bbangle.bbangle.push.service;

import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.push.domain.Push;
import com.bbangle.bbangle.push.domain.PushCategory;
import com.bbangle.bbangle.push.dto.PushRequest;
import com.bbangle.bbangle.push.dto.CreatePushRequest;
import com.bbangle.bbangle.push.dto.PushResponse;
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
        Push push = pushRepository.findPush(request.boardId(), request.pushCategory(), memberId);

        if (push == null) {
            Push newPush = Push.builder()
                    .fcmToken(request.fcmToken())
                    .memberId(memberId)
                    .boardId(request.boardId())
                    .pushCategory(PushCategory.valueOf(request.pushCategory()))
                    .pushStatus(false)
                    .build();

            pushRepository.save(newPush);
        } else {
            push.resubscribePush();
        }
    }


    public void cancelPush(PushRequest request, Long memberId) {
        memberRepository.findMemberById(memberId);
        Push push = pushRepository.findPush(request.boardId(), request.pushCategory(), memberId);
        push.unsubscribePush();
    }


    public void deletePush(PushRequest request, Long memberId) {
        memberRepository.findMemberById(memberId);
        Push push = pushRepository.findPush(request.boardId(), request.pushCategory(), memberId);
        pushRepository.delete(push);
    }


    public List<PushResponse> getPush(Long boardId, String pushCategory, Long memberId) {
        memberRepository.findMemberById(memberId);
        return pushRepository.findPushList(boardId, pushCategory, memberId);
    }

}
