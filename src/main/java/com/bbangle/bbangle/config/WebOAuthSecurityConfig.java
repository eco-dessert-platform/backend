package com.bbangle.bbangle.config;

import com.bbangle.bbangle.config.jwt.TokenAuthenticationFilter;
import com.bbangle.bbangle.config.jwt.TokenProvider;
import com.bbangle.bbangle.config.oauth.OAuth2AuthorizationReqBasedOnCookieRepository;
import com.bbangle.bbangle.config.oauth.OAuth2MemberCustomService;
import com.bbangle.bbangle.config.oauth.OAuth2SuccessHandler;
import com.bbangle.bbangle.repository.RefreshTokenRepository;
import com.bbangle.bbangle.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;


@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@Slf4j
public class WebOAuthSecurityConfig {

    private final OAuth2MemberCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberService memberService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.csrf(AbstractHttpConfigurer::disable);
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);



        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/token").permitAll()
                        .requestMatchers("/api/v1/search/**").permitAll()
                        .requestMatchers("/api/v1/landingPage").permitAll()
                        .requestMatchers("/api/v1/store/**").permitAll()
                        .requestMatchers("/api/v1/stores/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "api/v1/boards/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "api/v1/boards/folders/**").authenticated()

                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll());


        http.oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                        .authorizationRequestRepository(oAuth2AuthorizationReqBasedOnCookieRepository()))
                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserCustomService))
                .successHandler(oAuth2SuccessHandler()));

        http.logout(logout -> logout.logoutSuccessUrl("/login"));

        http.exceptionHandling(exp -> exp.defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                new AntPathRequestMatcher("/api/**")));



        return http.build();
    }


    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(tokenProvider,
                refreshTokenRepository,
                oAuth2AuthorizationReqBasedOnCookieRepository(),
                memberService
        );
    }


    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public OAuth2AuthorizationReqBasedOnCookieRepository oAuth2AuthorizationReqBasedOnCookieRepository(){
        return new OAuth2AuthorizationReqBasedOnCookieRepository();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
