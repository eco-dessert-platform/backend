package com.bbangle.bbangle.search.service.utils;

import com.bbangle.bbangle.common.redis.domain.RedisEnum;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeywordUtil {

    private static final String BEST_KEYWORD_KEY = "keyword";
    private static final String[] DEFAULT_SEARCH_KEYWORDS = {"글루텐프리", "비건", "저당", "키토제닉"};

    private final RedisRepository redisRepository;

    public void setBestKeywordInRedis(String[] keywords) {
        String bestKeywordNamespace = RedisEnum.BEST_KEYWORD.name();

        redisRepository.delete(bestKeywordNamespace, BEST_KEYWORD_KEY);
        if (keywords.length == 0) {
            redisRepository.set(bestKeywordNamespace, BEST_KEYWORD_KEY, DEFAULT_SEARCH_KEYWORDS);
            return;
        }

        redisRepository.set(bestKeywordNamespace, BEST_KEYWORD_KEY, keywords);
    }

    public List<String> getBestKeyword() {
        return redisRepository.getStringList(RedisEnum.BEST_KEYWORD.name(), BEST_KEYWORD_KEY);
    }

}
