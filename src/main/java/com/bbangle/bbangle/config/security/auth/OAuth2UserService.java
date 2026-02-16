package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.OAuth2Response;
import com.bbangle.bbangle.auth.oauth.client.OAuth2StateParser;
import com.bbangle.bbangle.auth.oauth.client.dto.CustomUserDetails;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.OAuthParams;
import com.bbangle.bbangle.auth.seller.facade.OAuth2SellerFacade;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2SellerFacade oAuth2SellerFacade;
    private final OAuth2StateParser stateParser;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = loadOAuth2User(request);
        OAuthParams dto = getParams();
        String registrationId = request.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = createOAuth2Response(registrationId, oAuth2User);

        return switch (dto.user()) {
            case CUSTOMER -> loginCustomer(oAuth2Response);
            case SELLER -> loginSeller(oAuth2Response);
        };
    }

    protected OAuth2User loadOAuth2User(OAuth2UserRequest request) {
        return super.loadUser(request);
    }

    protected OAuthParams getParams() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            throw new IllegalStateException("Must be called in HTTP request context");
        }
        HttpServletRequest request = servletAttributes.getRequest();

        return stateParser.getParams(request.getParameter("state"));
    }

    private OAuth2Response createOAuth2Response(String registrationId, OAuth2User oAuth2User) {
        OauthServerType serverType = OauthServerType.fromName(registrationId);
        OAuth2Response response = serverType.create(oAuth2User.getAttributes());

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

    private CustomUserDetails loginCustomer(OAuth2Response response) {
        Member member;
        // TODO : 추후에 결정되면 Customer 로그인 로직 추가
        throw new OAuth2Exception(BbangleErrorCode._NOT_SUPPORTED_YET);
    }
}
