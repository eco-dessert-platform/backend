package com.bbangle.bbangle.configuration;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class TokenGenerator extends AbstractIntegrationTest {

    @Autowired
    TokenProvider tokenProvider;

    @Test
    @DisplayName("토큰이 정상적으로 생성된다.")
    void generateTokenAndValidate() throws Exception {
        //given
        Member member = Member.builder().id(23L).build();
        String token = tokenProvider.generateToken(member, Duration.ofDays(1));

        //when
        boolean result = tokenProvider.isValidToken(token);

        //then
        assertThat(result).isTrue();
    }

}
