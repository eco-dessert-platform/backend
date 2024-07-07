package com.bbangle.bbangle.search.service.utils;

import com.bbangle.bbangle.board.dto.TitleDto;
import com.bbangle.bbangle.util.MorphemeAnalyzer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TitleUtil {

    private static final String WORD_SPACING = " ";

    public static Map<String, List<String>> getTitleBoardIdsMapping(List<TitleDto> titles) {
        Map<String, List<String>> mappingTitleIds = new HashMap<>();
        for (TitleDto titleDto : titles) {
            mappingTitleIds
                .computeIfAbsent(titleDto.getTitle(), k -> new ArrayList<>())
                .add(String.valueOf(titleDto.getBoardId()));
        }

        return mappingTitleIds;
    }

    public static List<String> getTitles(List<TitleDto> titleDtos) {
        return titleDtos.stream()
            .map(TitleDto::getTitle)
            .toList();
    }

    public static List<TitleDto> fromTokenizer(List<TitleDto> titleDtos,
        MorphemeAnalyzer morphemeAnalyzer) {
        List<TitleDto> tokenizedTitles = new ArrayList<>();
        for (TitleDto titleDto : titleDtos) {

            if (Objects.isNull(titleDto.getTitle())) {
                continue;
            }

            String lowerTitle = titleDto.getTitle().toLowerCase(Locale.ROOT);
            List<String> words = morphemeAnalyzer.getNounTokenizer(lowerTitle);
            List<String> splitWords = List.of(titleDto.getTitle().split(WORD_SPACING));
            words.addAll(splitWords);

            for (String word : words) {
                TitleDto wordTitleDto = new TitleDto(titleDto.getBoardId(), word);
                tokenizedTitles.add(wordTitleDto);
            }
        }

        return tokenizedTitles;
    }
}
