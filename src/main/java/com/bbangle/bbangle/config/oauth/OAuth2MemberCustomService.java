package com.bbangle.bbangle.config.oauth;

import com.bbangle.bbangle.dto.FolderRequestDto;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.service.WishListFolderService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OAuth2MemberCustomService extends DefaultOAuth2UserService {

    private static final String DEFAULT_FOLDER_NAME = "기본 폴더";

    private final MemberRepository memberRepository;
    private final WishListFolderService folderService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest); // 요청을 바탕으로 유저 정보를 담은 객체 반환
        Map<String, Object> originAttributes = user.getAttributes();  // OAuth2User의 attribute

        // OAuth2 서비스 id (google, kakao)
        String registrationId = userRequest.getClientRegistration()
            .getRegistrationId();
        OAuthAttributes oAuthAttributes = OAuthAttributes.of(registrationId, originAttributes);
        saveOrUpdate(oAuthAttributes);
        return user;
    }

    // 유저가 있으면 업데이트, 없으면 유저 생성
    @Transactional
    public Member saveOrUpdate(OAuthAttributes oAuthAttributes) {
        String provider = oAuthAttributes.getProvider();
        String email = oAuthAttributes.getEmail();
        String nickname = oAuthAttributes.getNickname();
        String profile = oAuthAttributes.getProfile();
        String name = oAuthAttributes.getName();
        Member member = null;
        if (provider.equals("google")) {
            member = memberRepository.findByEmail(email)
                .map(entity -> entity.updateNickname(name))
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                        .email(email)
                        .name(name)
                        .profile(profile)
                        .build();
                    memberRepository.save(newMember);
                    Long newMemberId = newMember.getId();
                    //기본 위시리스트 폴더 추가
                    folderService.create(newMemberId, new FolderRequestDto(DEFAULT_FOLDER_NAME));
                    return newMember;
                });
        } else if (provider.equals("kakao")) {
            //FIXME 닉네임은 중복될 가능성 높음, 추후 이메일과 이름의 권한 승인을 받아야 할 것으로 보임
            member = memberRepository.findByNickname(nickname)
                .map(entity -> entity.updateNickname(nickname))
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                        .nickname(nickname)
                        .profile(profile)
                        .build();
                    memberRepository.save(newMember);
                    Long newMemberId = newMember.getId();
                    //기본 위시리스트 폴더 추가
                    folderService.create(newMemberId, new FolderRequestDto(DEFAULT_FOLDER_NAME));
                    return newMember;
                });
        }
        return member;
    }

}

