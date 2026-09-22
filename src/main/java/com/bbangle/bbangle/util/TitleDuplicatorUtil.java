package com.bbangle.bbangle.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

/**
 * 복제 시 제목 중복을 피하기 위해 "제목 (n)" 형태의 다음 제목을 계산하는 유틸리티.
 * <p>
 * 예) "A"만 존재 -> "A (1)" <br>
 *     "A", "A (3)"이 존재 -> 중간 번호(1, 2)가 비어 있어도 가장 큰 값(3) 기준 +1인 "A (4)"
 */
@UtilityClass
public class TitleDuplicatorUtil {

    // "제목 (숫자)" 형태를 매칭하기 위한 정규식. group(1) = 순수 제목, group(2) = 숫자
    private static final Pattern SUFFIX_PATTERN = Pattern.compile("^(.*) \\((\\d+)\\)$");
    private static final int NO_MATCH = -1;
    private static final int BASE_TITLE_SUFFIX = 0;

    public String generateNextTitle(String originalTitle, List<String> existingTitles) {
        String baseTitle = extractBaseTitle(originalTitle);

        int maxSuffix = existingTitles.stream()
            .mapToInt(title -> extractSuffixIfMatchesBase(title, baseTitle))
            .filter(suffix -> suffix != NO_MATCH)
            .max()
            .orElse(BASE_TITLE_SUFFIX);

        return baseTitle + " (" + (maxSuffix + 1) + ")";
    }

    // "A (3)" 형태면 base인 "A"를 추출하고, 접미사가 없으면 원본 그대로 반환한다.
    private String extractBaseTitle(String title) {
        Matcher matcher = SUFFIX_PATTERN.matcher(title);
        return matcher.matches() ? matcher.group(1) : title;
    }

    // title이 baseTitle 자신이면 0, "baseTitle (n)" 형태면 n을 반환하고, 그 외에는 NO_MATCH(-1)를 반환한다.
    private int extractSuffixIfMatchesBase(String title, String baseTitle) {
        if (title.equals(baseTitle)) {
            return BASE_TITLE_SUFFIX;
        }

        Matcher matcher = SUFFIX_PATTERN.matcher(title);
        if (matcher.matches() && matcher.group(1).equals(baseTitle)) {
            return Integer.parseInt(matcher.group(2));
        }

        return NO_MATCH;
    }
}
