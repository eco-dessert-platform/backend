package com.bbangle.bbangle.config;

import com.bbangle.bbangle.common.service.RequestTimeInterceptor;
import com.bbangle.bbangle.config.security.resolver.LocalSecurityArgumentResolver;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RequestTimeInterceptor requestTimeInterceptor;
    private final OctetStreamReadMsgConverter octetStreamReadMsgConverter;
    private final Optional<LocalSecurityArgumentResolver> localSecurityArgumentResolver;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(octetStreamReadMsgConverter);
    }

    // Local 환경에서만 @AuthenticationPrincipal을 위한 ArgumentResolver 등록
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        localSecurityArgumentResolver.ifPresent(resolvers::add);
    }

    // 불필요한 swagger 로그 찍히는 부분 설정
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestTimeInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/swagger-resources/**",
                "/favicon.ico",
                "/error"
            );
    }

}
