package com.bbangle.bbangle.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@Getter
@Setter
@ToString
@Schema(description = "rest api 성공/실패 응답 바디")
public class CommonResult {

    @Schema(description = "응답 성공여부 : true/false", example = "true")
    private boolean success;

    @Schema(description = "응답 코드 번호 : >= 0 정상, < 0 비정상", example = "0")
    private int code;

    @Schema(description = "응답 메시지", example = "Success")
    private String message;

    @JsonIgnore
    private HttpStatus httpStatus = HttpStatus.OK;

    @JsonInclude(NON_EMPTY)
    @Schema(description = "필드 검증 에러 배열")
    private FieldError[] fieldErrors;

    public record FieldError(
            @Schema(description = "필드명", example = "username") String field,
            @Schema(description = "필드 검즈 에러 메세지", example = "must not be empty") String msg
    ) {
    }
}
