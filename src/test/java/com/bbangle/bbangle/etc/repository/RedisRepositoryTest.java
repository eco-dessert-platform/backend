package com.bbangle.bbangle.etc.repository;

import com.bbangle.bbangle.common.redis.domain.RedisEnum;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class RedisRepositoryTest {

    final String boardNameSpace = RedisEnum.BOARD.name();
    final String testKeyword = "비건";
    final String[] testBoardId = {"1", "2", "3", "4", "5"};
    @Autowired
    RedisRepository redisRepository;

    @AfterEach
    void after() {
        redisRepository.deleteAll();
    }

    @Test
    @DisplayName("Redis에 보드값을 저장하면 정상적으로 값을 가져올 수 있어야한다")
    void saveBoardValue() {
        redisRepository.set(boardNameSpace, testKeyword, testBoardId);
        var result = redisRepository.get(boardNameSpace, testKeyword);
        Assertions.assertEquals(5, result.size(), "결과값 개수가 모자랍니다");

        AtomicLong assertNumber = new AtomicLong(1L);
        result.forEach(boardId -> {
            Assertions.assertEquals(boardId, assertNumber.get(),
                String.format("%s 값이 다릅니다", boardId));
            assertNumber.getAndIncrement();
        });
    }

}
