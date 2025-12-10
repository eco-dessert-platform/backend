package com.bbangle.bbangle.auth.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class AdminRequest {

    public record AdminLoginRequest(
            @Schema(description = "관리자 계정 ID")
            @NotBlank(message = "관리자 계정 ID는 필수입니다.")
            String accountId,

            @Schema(description = "관리자 계정 비밀번호")
            @NotBlank(message = "관리자 계정 비밀번호는 필수입니다.")
            String password
    ) {
    }

}
