package com.bbangle.bbangle.common.service;

import static com.bbangle.bbangle.common.service.ResponseService.CommonResponse.FAIL;
import static com.bbangle.bbangle.common.service.ResponseService.CommonResponse.SUCCESS;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Component
public record ResponseService(MessageSource messageSource) {

    public <T> SingleResult<T> getSingleResult(T data) {
        SingleResult<T> result = new SingleResult<>();
        result.setResult(data);
        setSuccessResult(result);
        return result;
    }

    public <T> SingleResult<T> getSingleResult(Optional<T> opt) {
        return opt.map(t -> {
                SingleResult<T> result = new SingleResult<>();
                result.setResult(t);
                setSuccessResult(result);
                return result;
            })
            .orElse(getFailSingleResult());
    }

    public <T> SingleResult<T> getSingleResult(Optional<T> opt, String message, int code) {
        return opt.map(t -> {
                SingleResult<T> result = new SingleResult<>();
                result.setResult(t);
                setSuccessResult(result);
                return result;
            })
            .orElse(new SingleResult<T>(getFailResult(message, code)));
    }

    public <T> ListResult<T> getListResult(List<T> list) {
        ListResult<T> result = new ListResult<>();
        result.setList(list);
        setSuccessResult(result);
        return result;
    }

    public <T> ListResult<T> getListResult(List<T> list, boolean success, int code, String msg) {
        ListResult<T> result = new ListResult<>();
        result.setList(list);
        result.setSuccess(success);
        result.setCode(code);
        result.setMessage(msg);
        return result;
    }

    public CommonResult getSuccessResult() {
        CommonResult result = new CommonResult();
        setSuccessResult(result);
        return result;
    }

    private void setSuccessResult(CommonResult result) {
        result.setSuccess(true);
        result.setCode(SUCCESS.getCode());
        result.setMessage(SUCCESS.getMessage());
    }

    public SingleResult getFailSingleResult() {
        SingleResult result = new SingleResult();
        result.setSuccess(false);
        result.setCode(FAIL.getCode());
        result.setMessage(FAIL.getMessage());
        return result;
    }

    public CommonResult getFailResult() {
        CommonResult result = new CommonResult();
        result.setSuccess(false);
        result.setCode(FAIL.getCode());
        result.setMessage(FAIL.getMessage());
        return result;
    }

    public CommonResult getFailResult(@NonNull String msg, int code) {
        CommonResult result = new CommonResult();
        result.setSuccess(false);
        result.setCode(code);
        result.setMessage(
            messageSource.getMessage(msg, null, msg, LocaleContextHolder.getLocale()));
        return result;
    }

    public CommonResult getSuccessResult(@NonNull String msg, int code) {
        CommonResult result = new CommonResult();
        result.setSuccess(true);
        result.setCode(code);
        result.setMessage(
            messageSource.getMessage(msg, null, msg, LocaleContextHolder.getLocale()));
        return result;
    }

    public CommonResult getError(BbangleErrorCode error) {
        return getFailResult(error.getMessage(), error.getCode());
    }

    public CommonResult getMethodArgumentNotValidExceptionResult(MethodArgumentNotValidException ex) {
        CommonResult result = new CommonResult();
        result.setSuccess(false);
        CommonResult.FieldError[] fes = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> new CommonResult.FieldError(
                fe.getField(),
                messageSource.getMessage(
                    Objects.requireNonNull(fe.getDefaultMessage()),
                    null,
                    fe.getDefaultMessage(),
                    LocaleContextHolder.getLocale())))
            .toArray(CommonResult.FieldError[]::new);
        result.setFieldErrors(fes);

        if (Arrays.stream(result.getFieldErrors())
            .anyMatch(fieldError -> fieldError.field()
                .equals("category"))) {
            result.setCode(BbangleErrorCode.INVALID_CATEGORY.getCode());
            result.setMessage(BbangleErrorCode.INVALID_CATEGORY.getMessage());
        }
        return result;
    }

    public enum CommonResponse {
        SUCCESS(0, "SUCCESS"),
        FAIL(-1, "FAIL");

        final int code;
        final String message;

        CommonResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    public <T extends CommonResult> ResponseEntity<T> getResponseEntity(T result) {
        return new ResponseEntity<T>(result, result.getHttpStatus());
    }

}
