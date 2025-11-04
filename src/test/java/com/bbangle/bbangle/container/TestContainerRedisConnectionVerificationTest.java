package com.bbangle.bbangle.container;


import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
@DisplayName("테스트 컨테이너 Redis 연결 검증")
public class TestContainerRedisConnectionVerificationTest {

    @Qualifier("defaultRedisTemplate")
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Test
    @DisplayName("Redis 연결 및 기본 동작 확인")
    void testRedisConnection() {
        redisTemplate.opsForValue().set("key", "value");
        String value = (String) redisTemplate.opsForValue().get("key");
        log.info("Redis에서 조회한 값: {}", value);
        assertEquals("value", value);
    }
}
