package com.bbangle.bbangle.auth.oauth.service;

import com.bbangle.bbangle.auth.oauth.dto.CustomUserDetails;
import com.bbangle.bbangle.auth.oauth.dto.KakaoResponse;
import com.bbangle.bbangle.auth.oauth.dto.OAuth2Response;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.customer.service.MemberService;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(request);
        String registrationId = request.getClientRegistration().getRegistrationId();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("OAuth2User = " + gson.toJson(oAuth2User.getAttributes()));

        OAuth2Response oAuth2Response = null;
        switch (registrationId) {
            case "kakao" -> oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());

            case "google" -> throw new BbangleException("구글 로그인 구현 전");

            default -> throw new BbangleException("지원하지 않는 소셜 로그인 타입입니다.");
        }

        OAuth2Response finalOAuth2Response = oAuth2Response;
        Long memberId = memberRepository.findByProviderAndProviderId(oAuth2Response.getProvider(), oAuth2Response.getProviderId())
                .orElseGet(() -> memberService.getFirstJoinedMember(createMember(finalOAuth2Response)).getId());

        return CustomUserDetails.builder()
                .id(memberId)
                .role(Role.ROLE_SELLER)
                .name(oAuth2Response.getNickname())
                .build();
    }

    // TODO : 추후에 Seller 엔티티로 변경
    private Member createMember(OAuth2Response oAuth2Response) {
        return Member.builder()
                .providerId(oAuth2Response.getProviderId())
                .provider(oAuth2Response.getProvider())
                .email(oAuth2Response.getEmail())
                .nickname(oAuth2Response.getNickname())
                .profile(oAuth2Response.getProfile())
                .build();
    }
}
