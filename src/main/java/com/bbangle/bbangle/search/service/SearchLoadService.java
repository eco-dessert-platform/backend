package com.bbangle.bbangle.search.service;

import com.bbangle.bbangle.board.dto.TitleDto;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.common.redis.domain.RedisEnum;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.search.service.utils.AutoCompleteUtil;
import com.bbangle.bbangle.search.service.utils.KeywordUtil;
import com.bbangle.bbangle.search.service.utils.TitleUtil;
import com.bbangle.bbangle.util.MorphemeAnalyzer;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchLoadService {

    private static final int ONE_HOUR = 3_600_000;
    private static final String BOARD_MIGRATION = "board";
    private static final int ONEDAY = 24;
    private final BoardRepository boardRepository;
    private final ProductRepository productRepository;
    private final SearchRepository searchRepository;
    private final MorphemeAnalyzer morphemeAnalyzer;
    private final RedisRepository redisRepository;
    private final KeywordUtil keywordUtil;
    private final AutoCompleteUtil autoCompleteUtil;

    @PostConstruct
    @Scheduled(cron = "0 0 0 * * *")
    public void cacheKeywords() {
        boolean isTodayMigrationCompleted = checkTodayMigration();

        if (isTodayMigrationCompleted) { // 일에 한번만 업데이트
            return;
        }

        List<TitleDto> boardTitleDtos = boardRepository.findAllTitle();
        List<TitleDto> productTitleDtos = productRepository.findAllTitle();

        List<TitleDto> boardTokenizedTitles = TitleUtil.fromTokenizer(boardTitleDtos,
            morphemeAnalyzer);
        List<TitleDto> productTokenizedTitles = TitleUtil.fromTokenizer(productTitleDtos,
            morphemeAnalyzer);
        boardTokenizedTitles.addAll(productTokenizedTitles);

        Map<String, List<String>> mappingTitles = TitleUtil.getTitleBoardIdsMapping(
            boardTokenizedTitles);

        cacheKeyword(mappingTitles);
        setKeywordMigration();
    }

    private void cacheKeyword(Map<String, List<String>> mappingTitles) {
        redisRepository.setStringList(RedisEnum.BOARD.name(), mappingTitles);
    }

    private void setKeywordMigration() {
        redisRepository.setFromString(RedisEnum.MIGRATION.name(), BOARD_MIGRATION,
            LocalDateTime.now().toString());
    }

    @PostConstruct
    @Scheduled(cron = "0 0 0 * * *")
    public void cacheAutoComplete() {
        List<TitleDto> boardTitleDtos = boardRepository.findAllTitle();
        List<TitleDto> productTitleDtos = productRepository.findAllTitle();

        List<TitleDto> boardTokenizedTitles = TitleUtil.fromTokenizer(boardTitleDtos,
            morphemeAnalyzer);
        List<TitleDto> productTokenizedTitles = TitleUtil.fromTokenizer(productTitleDtos,
            morphemeAnalyzer);
        boardTokenizedTitles.addAll(productTokenizedTitles);

        List<String> titles = TitleUtil.getTitles(boardTokenizedTitles);
        autoCompleteUtil.insertAll(titles);
    }

    private boolean checkTodayMigration() {
        String migrationDateTime = redisRepository.getString(RedisEnum.MIGRATION.name(),
            BOARD_MIGRATION);

        if (!migrationDateTime.isEmpty()) {
            LocalDateTime migrationDate = LocalDateTime.parse(migrationDateTime,
                DateTimeFormatter.ISO_DATE_TIME);
            LocalDate today = LocalDate.now();

            if (migrationDate.toLocalDate().isEqual(today)) {
                return true;
            }
        }

        return false;
    }

    @Scheduled(fixedRate = ONE_HOUR)
    public void updateRedisAtBestKeyword() {
        LocalDateTime oneDayAgo = getOneDayAgo();
        String[] bestKeyword = searchRepository.getBestKeyword(oneDayAgo);
        keywordUtil.setBestKeywordInRedis(bestKeyword);
    }

    private LocalDateTime getOneDayAgo() {
        return LocalDateTime.now()
            .minusHours(ONEDAY);
    }
}
