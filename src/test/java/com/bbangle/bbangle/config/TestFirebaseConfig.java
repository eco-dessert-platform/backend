package com.bbangle.bbangle.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;

@Slf4j
@TestConfiguration
public class TestFirebaseConfig {

    @PostConstruct
    public void init() {
        log.info("FirebaseConfig init");
    }

}
