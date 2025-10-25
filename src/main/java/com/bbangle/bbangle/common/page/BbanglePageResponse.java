package com.bbangle.bbangle.common.page;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "페이지네이션 응답")
public record BbanglePageResponse<T>(
    @Schema(description = "응답 데이터 리스트") List<T> content,
    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0") int page,
    @Schema(description = "페이지 당 데이터 개수", example = "10") int size,
    @Schema(description = "총 페이지 수", example = "5") int totalPages,
    @Schema(description = "총 데이터 개수", example = "47") long totalElements
) {

    public static <T> BbanglePageResponse<T> of(Page<T> page) {
        return new BbanglePageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements()
        );
    }

}
