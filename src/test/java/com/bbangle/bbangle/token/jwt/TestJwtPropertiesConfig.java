package com.bbangle.bbangle.token.jwt;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestJwtPropertiesConfig {
    @Bean
    public JwtProperties jwtProperties() {
        JwtProperties props = new JwtProperties();
        props.setIssuer("test-issuer");
        props.setSecretKey("test-secret-key");
        return props;
    }
}