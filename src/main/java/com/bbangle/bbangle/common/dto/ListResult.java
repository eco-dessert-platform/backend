package com.bbangle.bbangle.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "rest api 배열 응답 바디")
public class ListResult<T> extends CommonResult {
    @Schema(description = "응답값 배열")
    private List<T> list;
}
