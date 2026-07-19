package com.bbangle.bbangle.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] FileNameUtil")
class FileNameUtilTest {

    @Test
    @DisplayName("사용된 적 없는 경로면 그대로 반환한다")
    void return_same_path_when_not_used() {
        // arrange
        Set<String> usedPaths = new HashSet<>();

        // act
        String result = FileNameUtil.resolveUniquePath("store/사업자등록증.pdf", usedPaths);

        // assert
        assertThat(result).isEqualTo("store/사업자등록증.pdf");
    }

    @Test
    @DisplayName("이미 사용된 경로면 (2) 접미사를 붙인다")
    void append_suffix_when_path_already_used() {
        // arrange
        Set<String> usedPaths = new HashSet<>(Set.of("store/사업자등록증.pdf"));

        // act
        String result = FileNameUtil.resolveUniquePath("store/사업자등록증.pdf", usedPaths);

        // assert
        assertThat(result).isEqualTo("store/사업자등록증(2).pdf");
    }

    @Test
    @DisplayName("(2)까지 사용 중이면 (3) 접미사를 붙인다")
    void append_incrementing_suffix_when_candidates_already_used() {
        // arrange
        Set<String> usedPaths = new HashSet<>(Set.of(
            "store/사업자등록증.pdf",
            "store/사업자등록증(2).pdf"
        ));

        // act
        String result = FileNameUtil.resolveUniquePath("store/사업자등록증.pdf", usedPaths);

        // assert
        assertThat(result).isEqualTo("store/사업자등록증(3).pdf");
    }
}
