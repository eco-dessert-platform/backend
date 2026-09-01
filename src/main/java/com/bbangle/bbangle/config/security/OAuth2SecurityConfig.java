package com.bbangle.bbangle.config.security;

import com.bbangle.bbangle.auth.oauth.client.OAuth2StateParser;
import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.config.logging.filter.LoggingFilter;
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
import org.springframework.core.env.Environment;
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

    @Bean
    @Order(1)
    public SecurityFilterChain oauth2FilterChain(
        HttpSecurity http,
        CustomOAuth2AuthorizationRequestResolver oAuth2Resolver,
        OAuth2ClientValidationFilter validationFilter,
        SlackAdaptor slackAdaptor
    ) throws Exception {
        http
            .securityMatcher(
                "/api/v1/oauth/authorization/**",
                "/login/oauth2/**"
            )
            .csrf(AbstractHttpConfigurer::disable)
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> endpoint
                    .authorizationRequestResolver(oAuth2Resolver))
                .userInfoEndpoint(config -> config.userService(oAuth2UserService))
                .successHandler(successHandler)
                .failureHandler(failureHandler)
            )
            .addFilterBefore(
                validationFilter,
                OAuth2AuthorizationRequestRedirectFilter.class
            )
            .addFilterBefore(
                new LoggingFilter(slackAdaptor),
                OAuth2ClientValidationFilter.class
            );
        return http.build();
    }

    @Bean
    public OAuth2ClientValidationFilter validationFilter(
        ClientRegistrationRepository clientRegistrationRepository,
        OAuth2HandlerProperties oauth2HandlerProperties,
        OAuth2StateParser stateParser,
        Environment environment
    ) {
        return new OAuth2ClientValidationFilter(
            clientRegistrationRepository,
            oauth2HandlerProperties,
            stateParser,
            environment
        );
    }

    @Bean
    public CustomOAuth2AuthorizationRequestResolver oAuth2Resolver(
        ClientRegistrationRepository clientRegistrationRepository
    ) {
        return new CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
    }
}
