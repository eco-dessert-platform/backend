package com.bbangle.bbangle.repository.impl;

import com.bbangle.bbangle.repository.RedisRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class RedisRepositoryTest {

    @Autowired
    RedisRepository redisRepository;
    @Test
    void getRedisValue(){
//        Object result = redisRepository.get("비");
//
//        // 결과가 List의 인스턴스인지 확인
//        Assertions.assertTrue(result instanceof List, "결과가 List의 인스턴스가 아닙니다");
//
//        // 리스트 내부 요소의 타입을 추가로 확인하려면 다음과 같이 할 수 있습니다:
//        Assertions.assertTrue(((List<?>) result).isEmpty() || ((List<?>) result).get(0) instanceof Long,
//                "리스트의 요소가 Long 타입이 아닙니다");
    }
}
