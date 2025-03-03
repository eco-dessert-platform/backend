package com.bbangle.bbangle.search.service;

import com.bbangle.bbangle.board.dto.TitleDto;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.search.repository.SearchRepository;
import com.bbangle.bbangle.search.service.utils.AutoCompleteUtil;
import com.bbangle.bbangle.search.service.utils.KeywordUtil;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchLoadService {

    private static final int ONE_HOUR = 3_600_000;
    private static final int ONEDAY = 24;
    private static final String WORD_SPACING = " ";

    private final SearchRepository searchRepository;
    private final KeywordUtil keywordUtil;
    private final BoardRepository boardRepository;
    private final BoardRepository productRepository;
    private final AutoCompleteUtil autoCompleteUtil;


    @PostConstruct
    @Scheduled(cron = "0 0 0 * * *")
    public void cacheAutoComplete() {
        List<TitleDto> boardTitleDtos = boardRepository.findAllTitle();
        List<TitleDto> productTitleDtos = productRepository.findAllTitle();

        List<TitleDto> tokenizedTitles = new ArrayList<>();
        tokenizedTitles.addAll(tokenizeTitles(boardTitleDtos));
        tokenizedTitles.addAll(tokenizeTitles(productTitleDtos));

        List<String> titles = tokenizedTitles.stream().map(TitleDto::getTitle).toList();

        autoCompleteUtil.insertAll(titles);
    }

    private List<TitleDto> tokenizeTitles(List<TitleDto> titleDtos) {
        return titleDtos.stream().filter(dto -> dto.getTitle() != null).flatMap(
            dto -> Arrays.stream(dto.getTitle().split(WORD_SPACING))
                .map(word -> new TitleDto(dto.getBoardId(), word))).toList();
    }

    @Scheduled(fixedRate = ONE_HOUR)
    public void updateRedisAtBestKeyword() {
        LocalDateTime oneDayAgo = getOneDayAgo();
        String[] bestKeyword = searchRepository.getBestKeyword(oneDayAgo);
        keywordUtil.setBestKeywordInRedis(bestKeyword);
    }

    private LocalDateTime getOneDayAgo() {
        return LocalDateTime.now().minusHours(ONEDAY);
    }
}
