package com.bbangle.bbangle.config;

import com.bbangle.bbangle.token.jwt.TokenAuthenticationFilter;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import com.bbangle.bbangle.token.oauth.OauthServerTypeConverter;
import com.bbangle.bbangle.member.service.MemberService;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
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

    private final TokenProvider tokenProvider;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new OauthServerTypeConverter());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedOrigins("http://www.bbangle.store")
                .allowedOrigins("https://api.bbangle.store")
                .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.PATCH.name()
                )
                .allowCredentials(true)
                .exposedHeaders("*");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.csrf(AbstractHttpConfigurer::disable);
        http.addFilterBefore(tokenAuthenticationFilter(),
            UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/api/token")
            .permitAll()
            .requestMatchers("/api/v1/oauth/**")
            .permitAll()
            .requestMatchers("/api/v1/search/**")
            .permitAll()
            .requestMatchers("/api/v1/landingpage")
            .permitAll()
            .requestMatchers("/api/v1/store/**")
            .permitAll()
            .requestMatchers("/api/v1/stores/**")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "api/v1/boards/**")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "api/v1/notice/**")
            .permitAll()
            .requestMatchers(HttpMethod.PATCH, "api/v1/boards/**")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "api/v1/boards/folders/**")
            .authenticated()
            //TODO: 글을 작성하는 경우에 ADMIN 계정만 가능하도록 설정이 필요 authority 에 대한 추가 설정이 필요한 것으로 보임
            .requestMatchers(HttpMethod.GET, "api/v1/boards/notification/**")
            .permitAll()

            .requestMatchers("/api/**")
            .authenticated()
            .anyRequest()
            .permitAll());

        http.logout(logout -> logout.logoutSuccessUrl("/login"));

        http.exceptionHandling(exp -> exp.defaultAuthenticationEntryPointFor(
            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
            new AntPathRequestMatcher("/api/**")));

        return http.build();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }



}
