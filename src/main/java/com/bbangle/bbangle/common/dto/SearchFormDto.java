package com.bbangle.bbangle.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.Sort;

public class SearchFormDto {

    @Data
    @NoArgsConstructor
    @ToString
    public static class KeywordSearchCondition {
        @Schema(description = "키워드")
        private List<String> keywords;

        @JsonIgnore
        public String getKeyword() {
            if (keywords != null && !keywords.isEmpty()) {
                return keywords.get(0);
            }
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @ToString
    public static class DefaultSearchCondition extends KeywordSearchCondition {

        @Schema(description = "다중 검색 이용 유무", example = "false")
        private Boolean isMultipleSearch;

        @NotNull
        @Schema(description = "검색 시작 날짜")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate startDate;

        @NotNull
        @Schema(description = "검색 종료 날짜")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private LocalDate endDate;

    }

    @Data
    @NoArgsConstructor
    public static class DefaultSortDirectionCondition {
        @Schema(description = "정렬 기준")
        private Sort.Direction sort = Sort.Direction.DESC;
    }

}
