package com.bbangle.bbangle.auth.seller.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateTokenRequest(
    @NotBlank(message = "generateToken은 필수입니다.")
    String generateToken
) {
}
