package com.bbangle.bbangle.config.security;

import static com.bbangle.bbangle.common.role.Role.ROLE_ADMIN;
import static com.bbangle.bbangle.common.role.Role.ROLE_CUSTOMER;
import static com.bbangle.bbangle.common.role.Role.ROLE_SELLER;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.bbangle.bbangle.config.security.jwt.TokenAuthenticationFilter;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class SecurityConfig {

    private final TokenProvider tokenProvider;

    @Profile("!local")
    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**")
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(
                new TokenAuthenticationFilter(tokenProvider),
                UsernamePasswordAuthenticationFilter.class
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
            .exceptionHandling(exp ->
                exp.defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")
                )
            );
        return http.build();
    }

    @Profile("local")
    @Bean
    @Order(2)
    public SecurityFilterChain localApiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**")
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(
                new TokenAuthenticationFilter(tokenProvider),
                UsernamePasswordAuthenticationFilter.class
            )
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll())
            .exceptionHandling(exp ->
                exp.defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")
                )
            );
        return http.build();
    }

}
