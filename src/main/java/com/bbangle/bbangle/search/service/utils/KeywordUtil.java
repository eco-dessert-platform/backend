package com.bbangle.bbangle.search.service.utils;

import com.bbangle.bbangle.common.redis.domain.RedisEnum;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.util.MorphemeAnalyzer;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeywordUtil {

    private final MorphemeAnalyzer morphemeAnalyzer;
    private final RedisRepository redisRepository;

    public List<Long> getBoardIds(String keyword) {
        List<String> keywordTokens = morphemeAnalyzer.getAllTokenizer(keyword);

        return keywordTokens.stream()
            .map(key -> redisRepository.get(RedisEnum.BOARD.name(), key))
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

}
