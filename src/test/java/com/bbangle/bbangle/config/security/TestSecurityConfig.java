package com.bbangle.bbangle.config.security;

import static com.bbangle.bbangle.common.role.Role.ROLE_ADMIN;
import static com.bbangle.bbangle.common.role.Role.ROLE_CUSTOMER;
import static com.bbangle.bbangle.common.role.Role.ROLE_SELLER;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.bbangle.bbangle.auth.oauth.OauthServerTypeConverter;
import com.bbangle.bbangle.config.security.jwt.TokenAuthenticationFilter;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Profile("test")
@RequiredArgsConstructor
@Configuration
@Slf4j
public class TestSecurityConfig implements WebMvcConfigurer {

    private static final String[] ALLOWED_ORIGINS = new String[]{
        "http://localhost:3000",
        "https://www.bbanggree.com",
        "https://api.bbanggree.com",
        "https://develop.bbanggree.com"
    };
    private final TokenProvider tokenProvider;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new OauthServerTypeConverter());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(ALLOWED_ORIGINS)
                .allowedMethods(GET.name(), POST.name(), PUT.name(), DELETE.name(), PATCH.name())
                .allowCredentials(true)
                .exposedHeaders("*");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(tokenAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PublicApiPath.ANY_METHOD).permitAll() // Public (모든 HTTP 메서드)
                        .requestMatchers(GET, PublicApiPath.GET_ONLY).permitAll() //Public (GET 전용)
                        .requestMatchers(PATCH, PublicApiPath.PATCH_OLLY).permitAll() // Public (PATCH 전용)
                        .requestMatchers(CustomerApiPath.ANY_METHOD)
                        .hasAuthority(ROLE_CUSTOMER.getRole()) // Customer API
                        .requestMatchers(SellerApiPath.ANY_METHOD).hasAuthority(ROLE_SELLER.getRole())  // Seller API
                        .requestMatchers(AdminApiPath.ANY_METHOD).hasAuthority(ROLE_ADMIN.getRole()) // Admin API
                        .requestMatchers("/api/**").authenticated() // 나머지 /api 하위는 인증 필요
                        .anyRequest().permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login"))
                .exceptionHandling(exp ->
                        exp.defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**"))
                );

        return http.build();
    }

    private TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
