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
                        "http://115.85.181.105:8000",
                        "http://landing.bbangle.store",
                        "http://api.bbangle.store",
                        "http://www.bbangle.store",
                        "https://api.bbangle.store",
                        "https://www.bbangle.store"
                    )
                    .allowedHeaders("*")
                    .exposedHeaders("ACCESS_KEY", "Authorization", "RefreshToken")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH")
                    .allowCredentials(true);
            }
        };
    }
}
