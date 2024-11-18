package com.bbangle.bbangle.board.service.component;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewCountComponent {

    private static final String VISIT_TRUE = "ture";
    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    public void visit(String viewCountKey) {
        if (Objects.nonNull(viewCountKey)) {
            updateViewCount(viewCountKey);
        }
    }

    private void updateViewCount(String viewCountKey) {
        redisTemplate.opsForValue()
            .set(viewCountKey, VISIT_TRUE);
    }
}
