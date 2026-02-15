package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.OAuth2Response;
import com.bbangle.bbangle.auth.oauth.client.dto.CustomUserDetails;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.OAuthParams;
import com.bbangle.bbangle.auth.oauth.client.google.dto.GoogleResponse;
import com.bbangle.bbangle.auth.oauth.client.kakao.dto.KakaoResponse;
import com.bbangle.bbangle.auth.seller.facade.OAuth2SellerFacade;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2SellerFacade oAuth2SellerFacade;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = loadOAuth2User(request);
        OAuthParams dto = getParams();
        String registrationId = request.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = createOAuth2Response(registrationId, oAuth2User);

        return switch (dto.getUser()) {
            case CUSTOMER -> throw new OAuth2Exception(BbangleErrorCode._NOT_SUPPORTED_YET);
            case SELLER -> loginSeller(oAuth2Response);
        };
    }

    protected OAuth2User loadOAuth2User(OAuth2UserRequest request) {
        return super.loadUser(request);
    }

    protected OAuthParams getParams() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            throw new IllegalStateException("RequestAttributes not found. Must be called in an HTTP request context.");
        }
        HttpServletRequest request = requestAttributes.getRequest();

        return OAuth2DTO.getParams(request, getClass());
    }

    private OAuth2Response createOAuth2Response(String registrationId, OAuth2User oAuth2User) {
        OAuth2Response response = switch (registrationId) {
            case "kakao" -> new KakaoResponse(oAuth2User.getAttributes());
            case "google" -> new GoogleResponse(oAuth2User.getAttributes());
            default -> throw new OAuth2Exception(BbangleErrorCode.NOT_SUPPORTED_SERVER);
        };

        if (response.getName() == null && response.getNickname() == null) {
            throw new OAuth2Exception(BbangleErrorCode.MISSING_NAME_NICKNAME);
        }

        return response;
    }

    private CustomUserDetails loginSeller(OAuth2Response response) {
        Seller seller;
        try {
            seller = oAuth2SellerFacade.login(SellerCreateCommand.from(response));
        } catch (Exception e) {
            throw new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR, e);
        }

        return CustomUserDetails.builder()
            .id(seller.getId())
            .role(Role.ROLE_SELLER)
            .name(seller.getName())
            .status(seller.getCertificationStatus())
            .build();
    }
}
