package com.bbangle.bbangle.search.service.utils;

import com.bbangle.bbangle.board.dto.TitleDto;
import com.bbangle.bbangle.util.MorphemeAnalyzer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TitleUtil {

    public static Map<String, List<String>> getTitleBoardIdsMapping(List<TitleDto> titles) {
        Map<String, List<String>> mappingTitleIds = new HashMap<>();
        for (TitleDto titleDto : titles) {
            mappingTitleIds
                .computeIfAbsent(titleDto.title(), k -> new ArrayList<>())
                .add(String.valueOf(titleDto.boardId()));
        }

        return mappingTitleIds;
    }

    public static List<String> getTitles(List<TitleDto> titleDtos) {
        return titleDtos.stream()
            .map(TitleDto::title)
            .toList();
    }

    public static List<TitleDto> fromTokenizer(List<TitleDto> titleDtos,
        MorphemeAnalyzer morphemeAnalyzer) {
        List<TitleDto> tokenizedTitles = new ArrayList<>();
        for (TitleDto titleDto : titleDtos) {
            List<String> titles = morphemeAnalyzer.getNounTokenizer(titleDto.title().toLowerCase(
                Locale.ROOT));

            for (String title : titles) {
                tokenizedTitles.add(new TitleDto(titleDto.boardId(), title));
            }

            titles = List.of(titleDto.title().split(" "));

            for (String title : titles) {
                tokenizedTitles.add(new TitleDto(titleDto.boardId(), title));
            }
        }

        return tokenizedTitles;
    }
}
