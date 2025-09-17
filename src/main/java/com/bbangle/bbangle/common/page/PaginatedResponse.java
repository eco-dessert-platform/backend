package com.bbangle.bbangle.common.page;

import com.bbangle.bbangle.common.dto.CommonResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginatedResponse<T> extends CommonResult {

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
