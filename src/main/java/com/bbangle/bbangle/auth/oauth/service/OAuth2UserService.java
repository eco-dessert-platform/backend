package com.bbangle.bbangle.auth.oauth.service;

import com.bbangle.bbangle.auth.oauth.client.dto.CustomUserDetails;
import com.bbangle.bbangle.auth.oauth.client.dto.KakaoResponse;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2Response;
import com.bbangle.bbangle.auth.seller.facade.OAuth2SellerFacade;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.exception.OAuth2Exception;
import com.bbangle.bbangle.seller.domain.OAuth2Seller;
import com.bbangle.bbangle.seller.seller.service.command.OAuth2ResponseCreateCommand;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

// TODO : Test
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2SellerFacade oAuth2SellerFacade;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(request);
        String registrationId = request.getClientRegistration().getRegistrationId();

        // TODO : 제거하기
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        log.debug("OAuth2User = {}", gson.toJson(oAuth2User.getAttributes()));

        OAuth2Response oAuth2Response = createOAuth2Response(registrationId, oAuth2User);

        OAuth2Seller seller;
        try {
            seller = oAuth2SellerFacade.login(OAuth2ResponseCreateCommand.from(oAuth2Response));
        } catch (BbangleException e) {
            throw new OAuth2Exception(e.getBbangleErrorCode(), e);
        } catch (Exception e) {
            throw new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR, e);
        }

        return CustomUserDetails.builder()
                .id(seller.getId())
                .role(Role.ROLE_SELLER)
                .name(oAuth2Response.getNickname())
                .build();
    }

    private OAuth2Response createOAuth2Response(String registrationId, OAuth2User oAuth2User) {
        return switch (registrationId) {
            case "kakao" -> new KakaoResponse(oAuth2User.getAttributes());
            // TODO : Google 로그인 구현
            case "google" -> throw new OAuth2Exception(BbangleErrorCode.NOT_SUPPORTED_SERVER);
            default -> throw new OAuth2Exception(BbangleErrorCode.NOT_SUPPORTED_SERVER);
        };
    }
}
