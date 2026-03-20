package com.bbangle.bbangle.store.seller.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class StoreRequest {

    public record UpdateStoreNameRequest(
        @Schema(description = "변경할 스토어명", example = "빵그리의 오븐 1호점")
        @Size(min = 3, max = 50, message = "스토어명은 1자 이상 50자 이하로 입력해주세요.")
        @NotBlank(message = "스토어명은 필수입니다.")
        String newName
    ) {}

    public record UpdateStoreDetailRequest(
        @Schema(description = "한줄소개", example = "건강한 재료로 만드는 비건 베이커리")
        String introduce,

        @Schema(description = "연락처", example = "01012345678")
        @Pattern(regexp = "^[0-9]{9,11}$", message = "연락처는 11자리 이하의 숫자만 입력 가능합니다.")
        @NotBlank(message = "연락처는 필수입니다.")
        String phoneNumber,

        @Schema(description = "서브 연락처", example = "01012345678")
        @Pattern(regexp = "^[0-9]{9,11}$", message = "서브 연락처는 11자리 이하의 숫자만 입력 가능합니다.")
        String subPhoneNumber,

        @Schema(description = "이메일", example = "user@example.com", format = "email")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @Schema(description = "판매자 주소", example = "(우편번호) 성남시 금광동 222-31")
        @NotBlank(message = "출고지 주소는 필수입니다.")
        String originAddress,

        @Schema(description = "판매자 상세 주소", example = "나동 202호")
        @Size(max = 50, message = "상세 주소는 50자까지 입력 가능합니다.")
        @NotBlank(message = "출고지 상세 주소는 필수입니다.")
        String originAddressDetail
    ) {}
}
