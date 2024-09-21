package com.bbangle.bbangle.config;

import com.bbangle.bbangle.exception.BbangleException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * firbase Admin SDK 사용시 필요
 * But, 현재는 FCMToken만 쓰므로 필요하지 않음
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${fcm.key-path}")
    private String FCM_SECRET_KEY_PATH;

    @PostConstruct
    public void init() {
        try {
            InputStream inputStream = new ClassPathResource(FCM_SECRET_KEY_PATH).getInputStream();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            log.info("error " + e.getMessage());
            throw new BbangleException(e);
        }
    }

}
