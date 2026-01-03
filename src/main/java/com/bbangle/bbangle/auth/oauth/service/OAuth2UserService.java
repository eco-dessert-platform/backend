package com.bbangle.bbangle.auth.oauth.service;

import com.bbangle.bbangle.auth.oauth.dto.CustomUserDetails;
import com.bbangle.bbangle.auth.oauth.dto.KakaoResponse;
import com.bbangle.bbangle.auth.oauth.dto.OAuth2Response;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import com.bbangle.bbangle.seller.domain.OAuth2Seller;
import com.bbangle.bbangle.seller.repository.OAuth2SellerRepository;
import com.bbangle.bbangle.seller.seller.service.OAuth2SellerService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

// TODO : Test
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2SellerService oAuth2SellerService;
    private final OAuth2SellerRepository oAuth2SellerRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(request);
        String registrationId = request.getClientRegistration().getRegistrationId();

        // TODO : 제거하기
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println("OAuth2User = " + gson.toJson(oAuth2User.getAttributes()));

        OAuth2Response oAuth2Response = null;
        switch (registrationId) {
            case "kakao" -> oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
            
            // TODO : Google 로그인 구현
            case "google" -> throw new OAuth2Exception(BbangleErrorCode.NOT_SUPPORTED_SERVER);

            default -> throw new OAuth2Exception(BbangleErrorCode.NOT_SUPPORTED_SERVER);
        }

        try {
            OAuth2Response finalOAuth2Response = oAuth2Response;
            OAuth2Seller seller = oAuth2SellerRepository.findByProviderAndProviderId(oAuth2Response.getProvider(), oAuth2Response.getProviderId())
                    .orElseGet(() -> oAuth2SellerService.createOAuth2Seller(finalOAuth2Response));

            return CustomUserDetails.builder()
                    .id(seller.getId())
                    .role(Role.ROLE_SELLER)
                    .name(oAuth2Response.getNickname())
                    .build();
        } catch (Exception e) {
            throw new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }
}
