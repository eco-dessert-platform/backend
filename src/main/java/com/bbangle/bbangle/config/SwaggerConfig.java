package com.bbangle.bbangle.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("빵그리 API서버")
                .description("본 문서는 외부에 유출하지 마세요.\n실제 서비스시에는 profile를 변경해 비활성시킵니다.")
                .version("1.0.0");

        OAuthFlows oAuthFlows = new OAuthFlows()
                .authorizationCode(new OAuthFlow()
                        .authorizationUrl("/oauth/authorize")
                        .tokenUrl("/oauth/token")
                        .refreshUrl("/oauth/refresh")
                        .scopes(new Scopes().addString("read", "읽기권한 부여")));

        SecurityScheme jwtSchemes = new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT");
        SecurityScheme oauthSchemes = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(oAuthFlows);

        return new OpenAPI()
                .info(info)
                .addSecurityItem(new SecurityRequirement().addList("카카오 토큰 로그인"))
                .addSecurityItem(new SecurityRequirement().addList("토큰 받아오기"))
                .components(new Components()
                        .addSecuritySchemes("카카오 토큰 로그인", jwtSchemes)
                        .addSecuritySchemes("토큰 받아오기", oauthSchemes)
                );
    }

    @Bean
    public WebMvcConfigurer forwardToIndex() {
        return new WebMvcConfigurer() {
            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                registry.addViewController("/").setViewName("redirect:/swagger-ui.html");
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("swagger-ui.html")
                        .addResourceLocations("classpath:/META-INF/resources/");
                registry.addResourceHandler("/webjars/**")
                        .addResourceLocations("classpath:/META-INF/resources/webjars/");
            }
        };
    }
}