package com.bbangle.bbangle.service;

import com.bbangle.bbangle.model.Member;
import com.bbangle.bbangle.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private static final long DEFAULT_MEMBER_ID = 1L;
    private static final String DEFAULT_MEMBER_NAME = "비회원";
    private static final String DEFAULT_MEMBER_NICKNAME = "비회원";
    private static final String DEFAULT_MEMBER_EMAIL = "example@xxxxx.com";

    private final MemberRepository memberRepository;

    @PostConstruct
    public void initSetting(){
        // 1L MemberId에 멤버는 무조건 비회원
        // 만약 1L 멤버가 없다면 비회원 멤버 생성
        memberRepository.findById(DEFAULT_MEMBER_ID).ifPresentOrElse(
                member -> log.info("Default member already exists"),
                () -> {
                    memberRepository.save(Member.builder()
                            .id(DEFAULT_MEMBER_ID)
                            .name(DEFAULT_MEMBER_NAME)
                            .nickname(DEFAULT_MEMBER_NICKNAME)
                            .email(DEFAULT_MEMBER_EMAIL)
                            .build());
                    log.info("Default member created");
                }
        );
    }

    public Member findById(Long id){
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("findById() >>>>> no Member by Id"));
    }

    public Member findByEmail(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("findByEmail() >>>> no Member by Email"));
    }

    public Member findByNickname(String nickname){
        return memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException(("findByNickname() >>>>> no Member by Nickname")));
    }
}
