package com.bbangle.bbangle.member.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class RequestEmailDto {

    @NotBlank(message = "이메일 입력은 필수 입니다.")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$",
        message = "이메일 형식이 유효하지 않습니다"
    )
    private String email;

}
