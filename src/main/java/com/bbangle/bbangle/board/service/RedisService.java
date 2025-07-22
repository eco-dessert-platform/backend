package com.bbangle.bbangle.board.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

    private static final String RANDOM_SET_PREFIX = "RANDOM:";
    private final Random random = new Random();

    @Qualifier("randomRedisTemplate")
    private final RedisTemplate<String, Integer> redisTemplate;

    public Integer getSetNumber(Long memberId) {
        if(redisTemplate.hasKey(RANDOM_SET_PREFIX + memberId)){
            return redisTemplate.opsForValue().get(RANDOM_SET_PREFIX + memberId);
        }

        int randomNumberSet = random.nextInt(1000) + 1;
        redisTemplate.opsForValue().set(RANDOM_SET_PREFIX + memberId, randomNumberSet, 1, TimeUnit.DAYS);

        return randomNumberSet;
    }

}
