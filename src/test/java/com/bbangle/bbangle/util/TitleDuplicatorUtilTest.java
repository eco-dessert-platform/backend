package com.bbangle.bbangle.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] TitleDuplicatorUtil")
public class TitleDuplicatorUtilTest {

    @Nested
    @DisplayName("generateNextTitle 메서드")
    class GenerateNextTitle {

        @Test
        @DisplayName("동일한 이름이 하나도 없으면 '(1)'을 붙인다")
        void returnsFirstSuffix_whenNoExistingTitle() {

            // given
            String originalTitle = "A";
            List<String> existingTitles = List.of("B", "C");

            // when
            String result = TitleDuplicatorUtil.generateNextTitle(originalTitle, existingTitles);

            // then
            assertThat(result).isEqualTo("A (1)");
        }

        @Test
        @DisplayName("원본 제목만 존재하면 '(1)'을 붙인다")
        void returnsFirstSuffix_whenOnlyBaseTitleExists() {

            // given
            String originalTitle = "A";
            List<String> existingTitles = List.of("A");

            // when
            String result = TitleDuplicatorUtil.generateNextTitle(originalTitle, existingTitles);

            // then
            assertThat(result).isEqualTo("A (1)");
        }

        @Test
        @DisplayName("중간 번호가 비어 있어도 가장 큰 번호를 기준으로 다음 번호를 생성한다")
        void returnsNextOfMaxSuffix_whenGapExists() {

            // given - "A"와 "A (3)"만 존재 (1, 2는 존재하지 않음)
            String originalTitle = "A";
            List<String> existingTitles = List.of("A", "A (3)");

            // when
            String result = TitleDuplicatorUtil.generateNextTitle(originalTitle, existingTitles);

            // then
            assertThat(result).isEqualTo("A (4)");
        }

        @Test
        @DisplayName("비슷하지만 다른 이름은 무시한다")
        void ignoresUnrelatedSimilarTitles() {

            // given
            String originalTitle = "A";
            List<String> existingTitles = List.of("AB", "A (오류)", "A (1) 수정본");

            // when
            String result = TitleDuplicatorUtil.generateNextTitle(originalTitle, existingTitles);

            // then
            assertThat(result).isEqualTo("A (1)");
        }

        @Test
        @DisplayName("복제 대상 자체가 이미 '(n)' 형태의 제목이어도 같은 base 제목을 기준으로 계산한다")
        void extractsBaseTitle_whenOriginalAlreadyHasSuffix() {

            // given - "A (2)"를 복제하는 상황이고, 이미 "A (5)"까지 존재
            String originalTitle = "A (2)";
            List<String> existingTitles = List.of("A", "A (2)", "A (5)");

            // when
            String result = TitleDuplicatorUtil.generateNextTitle(originalTitle, existingTitles);

            // then
            assertThat(result).isEqualTo("A (6)");
        }
    }
}
