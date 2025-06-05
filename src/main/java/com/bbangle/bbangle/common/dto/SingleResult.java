package com.bbangle.bbangle.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SingleResult<T> extends CommonResult {

    private T result;

    public SingleResult(CommonResult commonResult) {
        this.setMessage(commonResult.getMessage());
        this.setCode(commonResult.getCode());
        this.setSuccess(commonResult.isSuccess());
        this.setFieldErrors(commonResult.getFieldErrors());
    }
}
