package com.bbangle.bbangle.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.bbangle.bbangle.token.jwt.TokenAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final CookieCsrfTokenRepository cookieCsrfTokenRepository;
    private final CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .csrf(csrf -> csrf
                .csrfTokenRepository(cookieCsrfTokenRepository)
                .csrfTokenRequestHandler(csrfTokenRequestAttributeHandler)
                .ignoringRequestMatchers(
                    "/api/v1/oauth/**",
                    "/api/v1/health/**",
                    "/api/v1/token",
                    "/swagger-ui.html"
                )

            )
            .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authorize ->
                authorize
                    .requestMatchers("/api/v1/token").permitAll()
                    .requestMatchers("/api/v1/oauth/**").permitAll()
                    .requestMatchers("/api/v1/search/**").permitAll()
                    .requestMatchers("/api/v1/landingpage").permitAll()
                    .requestMatchers("/api/v1/store/**").permitAll()
                    .requestMatchers("/api/v1/stores/**").permitAll()
                    .requestMatchers("/api/v1/health/**").permitAll()
                    .requestMatchers("/api/v1/push/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/boards/**").permitAll()
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/boards/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/notification/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/boards/notification/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/review/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/analytics/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/boards/folders/**").authenticated()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
            )
            .exceptionHandling(exp -> exp
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")
                )
            );

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 

