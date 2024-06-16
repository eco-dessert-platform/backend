package com.bbangle.bbangle.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "rest api 하나 값 응답 바디")
public class SingleResult<T> extends CommonResult {

    @Schema(description = "응답값", example = "Here could be an example value")
    private T result;

    public SingleResult(CommonResult commonResult) {
        this.setMessage(commonResult.getMessage());
        this.setCode(commonResult.getCode());
        this.setSuccess(commonResult.isSuccess());
        this.setFieldErrors(commonResult.getFieldErrors());
    }
}
