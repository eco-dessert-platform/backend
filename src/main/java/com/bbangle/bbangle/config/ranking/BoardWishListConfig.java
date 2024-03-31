package com.bbangle.bbangle.config.ranking;

import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.util.RedisKeyUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@RequiredArgsConstructor
public class BoardWishListConfig {

    private final BoardRepository boardRepository;

    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        boardRepository.findAll()
            .forEach(board -> {
                redisTemplate.opsForZSet()
                    .add(RedisKeyUtil.RECOMMEND_KEY, String.valueOf(board.getId()), 0);
                redisTemplate.opsForZSet()
                    .add(RedisKeyUtil.POPULAR_KEY, String.valueOf(board.getId()), 0);
            });
    }

}
