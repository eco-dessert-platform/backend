package com.bbangle.bbangle.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    private static final long ONE_HOUR = 3600L;

    // Spring Security의 cors(Customizer.withDefaults())가 이 빈을 자동으로 찾아 사용
    @Bean
    @Profile({"dev", "local"})
    public CorsConfigurationSource devCorsConfigurationSource() {
        CorsConfiguration config = createBaseCorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:5000",
            "http://localhost:3000",
            "http://localhost:63342",
            "http://localhost:8001",
            "http://localhost:8000",
            "http://dev.bbanggree.com",
            "https://dev.bbanggree.com",
            "http://develop.bbanggree.com",
            "https://develop.bbanggree.com",
            "http://local.bbanggree.com:3000"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    @Profile("production")
    public CorsConfigurationSource prodCorsConfigurationSource() {
        CorsConfiguration config = createBaseCorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
            "https://www.bbanggree.com",
            "https://api.bbanggree.com",
            "https://master.d2xvuesi0d3ssg.amplifyapp.com"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private CorsConfiguration createBaseCorsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("ACCESS_KEY", "Authorization", "RefreshToken"));
        config.setAllowCredentials(true);
        config.setMaxAge(ONE_HOUR);
        return config;
    }
}
