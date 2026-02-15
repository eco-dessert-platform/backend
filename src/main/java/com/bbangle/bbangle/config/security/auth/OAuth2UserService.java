package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.OAuth2Response;
import com.bbangle.bbangle.auth.oauth.client.dto.CustomUserDetails;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2Redis.OAuthParams;
import com.bbangle.bbangle.auth.oauth.client.google.dto.GoogleResponse;
import com.bbangle.bbangle.auth.oauth.client.kakao.dto.KakaoResponse;
import com.bbangle.bbangle.auth.seller.facade.OAuth2SellerFacade;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    public static final String OAUTH_STATE_NAMESPACE = "oauth2:params";

    private final OAuth2SellerFacade oAuth2SellerFacade;
    private final RedisRepository redisRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {

        OAuthParams dto = getParams(
            (ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())
        );

        OAuth2User oAuth2User = loadOAuth2User(request);
        String registrationId = request.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = createOAuth2Response(registrationId, oAuth2User);

        if (oAuth2Response.getName() == null && oAuth2Response.getNickname() == null) {
            throw new OAuth2Exception(BbangleErrorCode.MISSING_NAME_NICKNAME);
        }

        return switch (dto.user()) {
            case "seller" -> createCustomSellerDetails(oAuth2Response, dto);
            case "customer" -> createCustomCustomerDetails(oAuth2Response, dto);
            default -> throw new OAuth2Exception(BbangleErrorCode.OAUTH_INVALID_PARAMS);
        };
    }

    protected OAuth2User loadOAuth2User(OAuth2UserRequest request) {
        return super.loadUser(request);
    }

    private OAuthParams getParams(ServletRequestAttributes requestAttributes) {
        HttpServletRequest request = requestAttributes.getRequest();
        String state = request.getParameter("state");

        OAuthParams dto = redisRepository.getDTO(OAUTH_STATE_NAMESPACE, state, OAuthParams.class);

        if (dto == null) throw new OAuth2Exception(BbangleErrorCode.OAUTH_INVALID_PARAMS);

        return dto;
    }

    private OAuth2Response createOAuth2Response(String registrationId, OAuth2User oAuth2User) {
        return switch (registrationId) {
            case "kakao" -> new KakaoResponse(oAuth2User.getAttributes());
            case "google" -> new GoogleResponse(oAuth2User.getAttributes());
            default -> throw new OAuth2Exception(BbangleErrorCode.NOT_SUPPORTED_SERVER);
        };
    }

    private CustomUserDetails createCustomSellerDetails(OAuth2Response oAuth2Response, OAuthParams params) {
        Seller seller;
        try {
            seller = oAuth2SellerFacade.login(SellerCreateCommand.from(oAuth2Response));
        } catch (Exception e) {
            throw new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR, e);
        }

        return CustomUserDetails.builder()
            .id(seller.getId())
            .role(Role.ROLE_SELLER)
            .name(seller.getName())
            .status(seller.getCertificationStatus())
            .params(params)
            .build();
    }

    // TODO : Customer 로그인 로직 추가하기
    private CustomUserDetails createCustomCustomerDetails(OAuth2Response oAuth2Response, OAuthParams params) {
/*        Member member = null;
        try {
            // Customer OAuth 로그인은 추후에 CustomerOauthService.login() 메서드를 리팩토링하여 사용
            // Customer도 Seller와 마찬가지로 providerId, provider, name or nickname, profileImg만 저장함
            // CustomerOauthService.login()에서 Member 엔티티를 반환하도록 변경
            CustomerOauthService.login();
        } catch (Exception e) {
            throw new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR, e);
        }

        return CustomUserDetails.builder()
            .id(member.getId())
            .role(Role.ROLE_CUSTOMER)
            .name(member.getName())
            .status(null)
            .params(params)
            .build();*/

        throw new OAuth2Exception(BbangleErrorCode._NOT_SUPPORTED_YET,
            new UnsupportedOperationException("Customer 로그인 기능은 아직 구현되지 않았습니다."));
    }
}
