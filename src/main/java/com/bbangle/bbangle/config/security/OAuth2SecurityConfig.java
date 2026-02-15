package com.bbangle.bbangle.config.security;

import com.bbangle.bbangle.config.security.auth.CustomFailureHandler;
import com.bbangle.bbangle.config.security.auth.CustomOAuth2AuthorizationRequestResolver;
import com.bbangle.bbangle.config.security.auth.CustomSuccessHandler;
import com.bbangle.bbangle.config.security.auth.OAuth2ClientValidationFilter;
import com.bbangle.bbangle.config.security.auth.OAuth2HandlerProperties;
import com.bbangle.bbangle.config.security.auth.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;

@Profile("!test")
@RequiredArgsConstructor
@Configuration
public class OAuth2SecurityConfig {

    private final OAuth2UserService oAuth2UserService;
    private final CustomSuccessHandler successHandler;
    private final CustomFailureHandler failureHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2HandlerProperties oAuth2HandlerProperties;
    private final CustomOAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver;

    @Bean
    @Order(1)
    public SecurityFilterChain oauth2FilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(
                SellerApiPath.PREFIX + "/oauth2/**",
                "/login/oauth2/**"
            )
            .csrf(AbstractHttpConfigurer::disable)
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> endpoint
                    .authorizationRequestResolver(oAuth2AuthorizationRequestResolver)
                )
                .userInfoEndpoint(config ->
                    config.userService(oAuth2UserService))
                .successHandler(successHandler)
                .failureHandler(failureHandler)
            )
            .addFilterBefore(
                validationFilter(),
                OAuth2AuthorizationRequestRedirectFilter.class
            );
        return http.build();
    }

    @Bean
    public OAuth2ClientValidationFilter validationFilter() {
        return new OAuth2ClientValidationFilter(
            clientRegistrationRepository,
            oAuth2HandlerProperties
        );
    }
}
