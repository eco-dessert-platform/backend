package com.bbangle.bbangle.common.page;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

// TODO : 공통 페이징 로직에 따라서 사용여부 결정
@Getter
@Setter
public class PaginatedResponse<T> {

    private List<T> content;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int pageNumber;

    @Schema(description = "Page size", example = "10")
    private int pageSize;

    @Schema(description = "Total number of pages", example = "5")
    private int totalPages;

    @Schema(description = "Total number of elements", example = "48")
    private long totalElements;

}
