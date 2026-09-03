package com.bbangle.bbangle.config.security;

import static com.bbangle.bbangle.common.role.Role.ROLE_ADMIN;
import static com.bbangle.bbangle.common.role.Role.ROLE_CUSTOMER;
import static com.bbangle.bbangle.common.role.Role.ROLE_SELLER;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.logging.filter.LoggingFilter;
import com.bbangle.bbangle.config.security.filter.ExceptionHandlerFilter;
import com.bbangle.bbangle.config.security.handler.BbangleAccessDeniedHandler;
import com.bbangle.bbangle.config.security.handler.BbangleAuthenticationEntryPoint;
import com.bbangle.bbangle.config.security.jwt.TokenAuthenticationFilter;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class SecurityConfig {

    private final TokenProvider tokenProvider;

    @Bean
    public BbangleAuthenticationEntryPoint bbangleAuthenticationEntryPoint(
        ResponseService responseService,
        ObjectMapper objectMapper
    ) {
        return new BbangleAuthenticationEntryPoint(responseService, objectMapper);
    }

    @Bean
    public BbangleAccessDeniedHandler bbangleAccessDeniedHandler(
        ResponseService responseService,
        ObjectMapper objectMapper
    ) {
        return new BbangleAccessDeniedHandler(responseService, objectMapper);
    }

    @Profile("!local")
    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(
        HttpSecurity http,
        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver,
        BbangleAuthenticationEntryPoint authenticationEntryPoint,
        BbangleAccessDeniedHandler accessDeniedHandler,
        SlackAdaptor slackAdaptor
    ) throws Exception {
        http
            .securityMatcher("/**")
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(
                new TokenAuthenticationFilter(tokenProvider),
                UsernamePasswordAuthenticationFilter.class
            )
            .addFilterBefore(
                new ExceptionHandlerFilter(handlerExceptionResolver),
                TokenAuthenticationFilter.class
            )
            .addFilterAfter(
                new LoggingFilter(slackAdaptor),
                ExceptionHandlerFilter.class
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/.well-known/**").permitAll() // Chrome DevTools 및 well-known URI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll() // Swagger
                .requestMatchers(PublicApiPath.ANY_METHOD).permitAll() // Public (모든 HTTP 메서드)
                .requestMatchers(GET, PublicApiPath.GET_ONLY).permitAll() //Public (GET 전용)
                .requestMatchers(PATCH, PublicApiPath.PATCH_OLLY).permitAll() // Public (PATCH 전용)
                .requestMatchers(CustomerApiPath.ANY_METHOD)
                .hasAuthority(ROLE_CUSTOMER.getRole()) // Customer API
                .requestMatchers(SellerApiPath.ANY_METHOD).hasAuthority(ROLE_SELLER.getRole()) // Seller API
                .requestMatchers(AdminApiPath.ANY_METHOD).hasAuthority(ROLE_ADMIN.getRole()) // Admin API
                .requestMatchers("/api/**").authenticated() // 나머지 /api 하위는 인증 필요
                .anyRequest().permitAll())
            .exceptionHandling(exp -> exp
                .defaultAuthenticationEntryPointFor(
                    authenticationEntryPoint,
                    new AntPathRequestMatcher("/api/**")
                )
                .accessDeniedHandler(accessDeniedHandler)
            );
        return http.build();
    }

    @Profile("local")
    @Bean
    @Order(2)
    public SecurityFilterChain localApiFilterChain(
        HttpSecurity http,
        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver,
        BbangleAuthenticationEntryPoint authenticationEntryPoint,
        BbangleAccessDeniedHandler accessDeniedHandler,
        SlackAdaptor slackAdaptor
    ) throws Exception {
        http
            .securityMatcher("/**")
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(
                new TokenAuthenticationFilter(tokenProvider),
                UsernamePasswordAuthenticationFilter.class
            )
            .addFilterBefore(
                new ExceptionHandlerFilter(handlerExceptionResolver),
                TokenAuthenticationFilter.class
            )
            .addFilterAfter(
                new LoggingFilter(slackAdaptor),
                ExceptionHandlerFilter.class
            )
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll())
            .exceptionHandling(exp -> exp
                .defaultAuthenticationEntryPointFor(
                    authenticationEntryPoint,
                    new AntPathRequestMatcher("/api/**")
                )
                .accessDeniedHandler(accessDeniedHandler)
            );
        return http.build();
    }

}
