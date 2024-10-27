package com.bbangle.bbangle.search.service.utils;

import static com.bbangle.bbangle.search.validation.SearchValidation.checkNullOrEmptyKeyword;

import com.bbangle.bbangle.common.redis.domain.RedisEnum;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeywordUtil {

    private static final String BEST_KEYWORD_KEY = "keyword";
    private static final String[] DEFAULT_SEARCH_KEYWORDS = {"글루텐프리", "비건", "저당", "키토제닉"};

    private final Tokenizer tokenizer;
    private final RedisRepository redisRepository;

    public List<Long> getBoardIds(String keyword) {
        checkNullOrEmptyKeyword(keyword);

        List<String> keywordTokens = tokenizer.getAllTokenizer(keyword);

        return keywordTokens.stream()
            .map(key -> redisRepository.get(RedisEnum.BOARD.name(), key))
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

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
