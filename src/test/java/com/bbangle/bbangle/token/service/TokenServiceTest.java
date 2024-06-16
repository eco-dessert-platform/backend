package com.bbangle.bbangle.token.service;

import static org.mockito.BDDMockito.given;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import com.bbangle.bbangle.token.oauth.OauthService;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import com.bbangle.bbangle.token.oauth.domain.client.OauthMemberClientComposite;
import com.bbangle.bbangle.token.oauth.infra.kakao.dto.LoginTokenResponse;
import net.datafaker.Faker;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class TokenServiceTest extends AbstractIntegrationTest {

    private static final Faker faker = new Faker();

    @Autowired
    OauthService oauthService;

    @Autowired
    TokenService tokenService;

    @Autowired
    TokenProvider tokenProvider;

    @MockBean
    OauthMemberClientComposite oauthMemberClientComposite;

    @Test
    @DisplayName("로그인 후 리프레시 토큰을 요청하면 정상적으로 리프레시 토큰을 반환한다.")
    void refreshSuccessTest() {
        //given
        Member kakaoMember = MemberFixture.createKakaoMember();
        String token = faker.random()
            .toString();
        given(oauthMemberClientComposite.fetch(OauthServerType.KAKAO, token)).willReturn(kakaoMember);
        LoginTokenResponse login = oauthService.login(OauthServerType.KAKAO, token);
        Object loginPrincipal = tokenProvider.getAuthentication(login.accessToken())
            .getPrincipal();

        //when
        String newAccessToken = tokenService.createNewAccessToken(login.refreshToken());

        //then
        Assertions.assertThat(tokenProvider.isValidToken(newAccessToken)).isTrue();
        Assertions.assertThat(tokenProvider.getAuthentication(newAccessToken).getPrincipal()).isEqualTo(loginPrincipal);
    }

}
