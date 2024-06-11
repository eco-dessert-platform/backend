package com.bbangle.bbangle.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.bbangle.bbangle.token.jwt.TokenAuthenticationFilter;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import com.bbangle.bbangle.token.oauth.OauthServerTypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@RequiredArgsConstructor
@Configuration
@Slf4j
public class WebOAuthSecurityConfig implements WebMvcConfigurer {

    private static final String[] ALLOWED_ORIGINS = new String[]{
        "http://localhost:3000",
        "http://www.bbangle.store",
        "http://api.bbangle.store",
        "http://115.85.181.105:8000",
        "https://www.bbangle.store",
        "https://api.bbangle.store"
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
            .authorizeHttpRequests(authorize ->
                authorize.requestMatchers("/api/v1/token").permitAll()
                    .requestMatchers("/api/v1/oauth/**").permitAll()
                    .requestMatchers("/api/v1/search/**").permitAll()
                    .requestMatchers("/api/v1/landingpage").permitAll()
                    .requestMatchers("/api/v1/store/**").permitAll()
                    .requestMatchers("/api/v1/stores/**").permitAll()
                    .requestMatchers("/api/v1/health/**").permitAll()
                    .requestMatchers(GET, "/api/v1/boards/**").permitAll()
                    .requestMatchers(PATCH, "/api/v1/boards/**").permitAll()
                    .requestMatchers(GET, "/api/v1/notification/**").permitAll()
                    //TODO: 글을 작성하는 경우에 ADMIN 계정만 가능하도록 설정이 필요 authority 에 대한 추가 설정이 필요한 것으로 보임
                    .requestMatchers(GET, "/api/v1/boards/notification/**").permitAll()
                    .requestMatchers(GET, "/api/v1/review/**").permitAll()
                    .requestMatchers(GET, "/api/v1/boards/folders/**").authenticated()
                    .requestMatchers("/api/**").authenticated()
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
