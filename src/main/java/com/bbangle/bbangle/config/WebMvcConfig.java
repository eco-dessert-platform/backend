package com.bbangle.bbangle.config;

import com.bbangle.bbangle.token.oauth.OauthServerTypeConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * ENUM명(대문자)이 아니어도 api 경로 가능케 하기 위함
     * ex) api/v1/login/kakao 가능하게 함
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new OauthServerTypeConverter());
    }
} 
