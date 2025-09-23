package com.bbangle.bbangle.common.page;

import com.bbangle.bbangle.common.dto.CommonResult;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

// TODO : 공통 페이징 로직에 따라서 사용여부 결정
@Getter
@Setter
public class PaginatedResponse<T> extends CommonResult {

    private List<T> content;

    private int pageNumber;

    private int pageSize;

    private int totalPages;

    private long totalElements;

}
