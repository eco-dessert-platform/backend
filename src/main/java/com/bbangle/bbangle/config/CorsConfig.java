package com.bbangle.bbangle.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOriginPatterns(
                        "http://localhost:5000",
                        "http://localhost:3000",
                        "http://localhost:63342",
                        "https://dev.bbanggree.com",
                        "https://www.bbanggree.com",
                        "https://api.bbanggree.com",
                        "https://master.d2xvuesi0d3ssg.amplifyapp.com"
                    )
                    .allowedHeaders("*")
                    .exposedHeaders("ACCESS_KEY", "Authorization", "RefreshToken")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH")
                    .allowCredentials(true);
            }
        };
    }
}
