package com.bbangle.bbangle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    private static final int ONE_HOUR = 60 * 60 * 1000;

    @Bean
    @Profile({"dev", "default"})
    public WebMvcConfigurer devCorsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(
                                "http://localhost:3000",
                                "http://localhost:5000",
                                "http://localhost:8000",
                                "http://localhost:8001",
                                "http://localhost:63342",
                                "http://dev.bbanggree.com",
                                "https://dev.bbanggree.com",
                                "http://develop.bbanggree.com",
                                "https://develop.bbanggree.com",
                                "http://local.bbanggree.com:3000"
                        )
                        .allowedMethods(
                                HttpMethod.GET.name(),
                                HttpMethod.POST.name(),
                                HttpMethod.PUT.name(),
                                HttpMethod.DELETE.name(),
                                HttpMethod.PATCH.name(),
                                HttpMethod.OPTIONS.name()
                        )
                        .allowedHeaders("*")
                        .exposedHeaders("X-XSRF-TOKEN")
                        .allowCredentials(true)
                        .maxAge(ONE_HOUR);
            }
        };
    }

    @Bean
    @Profile("production")
    public WebMvcConfigurer prodCorsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(
                                "https://www.bbanggree.com",
                                "https://api.bbanggree.com",
                                "https://master.d2xvuesi0d3ssg.amplifyapp.com"
                        )
                        .allowedMethods(
                                HttpMethod.GET.name(),
                                HttpMethod.POST.name(),
                                HttpMethod.PUT.name(),
                                HttpMethod.DELETE.name(),
                                HttpMethod.PATCH.name(),
                                HttpMethod.OPTIONS.name()
                        )
                        .allowedHeaders("*")
                        .exposedHeaders( "X-XSRF-TOKEN")
                        .allowCredentials(true)
                        .maxAge(ONE_HOUR);
            }
        };
    }
}
