package com.bbangle.bbangle.seller.seller.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.bbangle.bbangle.seller.seller.facade.command.RegisterDocumentsCommand;
import com.bbangle.bbangle.seller.seller.service.command.VerifyAccountCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public class SellerRequest {

    @Schema(description = "계좌 인증 요청 DTO")
    public record AccountVerificationRequest(
        @Schema(description = "은행코드 (토스페이먼츠 표준)", example = "92")
        @NotBlank(message = "은행코드는 필수입니다.")
        String bankCode,

        @Schema(description = "계좌번호", example = "123412341234")
        @NotBlank(message = "계좌번호는 필수입니다.")
        String accountNumber
    ) {

        public VerifyAccountCommand toCommand(Long sellerId) {
            return new VerifyAccountCommand(sellerId, bankCode, accountNumber);
        }
    }

    public record SellerDocumentsRegisterRequest(
        @Schema(description = "사업자 등록증", requiredMode = REQUIRED, type = "string", format = "binary")
        @NotNull(message = "사업자 등록증은 필수 입니다.")
        MultipartFile businessLicense,

        @Schema(description = "통신판매업 신고증", requiredMode = REQUIRED, type = "string", format = "binary")
        @NotNull(message = "통신판매업 신고증은 필수 입니다.")
        MultipartFile mailOrderLicense,

        @Schema(description = "개인명의 통장 사본", requiredMode = REQUIRED, type = "string", format = "binary")
        @NotNull(message = "통장 사본은 필수 입니다.")
        MultipartFile bankbookCopy,

        @Schema(description = "즉석식품제조가공업 & 식품제조업", requiredMode = REQUIRED, type = "string", format = "binary")
        @NotNull(message = "즉석식품제조가공업 & 식품제조업은 필수 입니다.")
        MultipartFile foodManufactureLicense
    ) {

        public RegisterDocumentsCommand toCommand(Long sellerId) {
            return RegisterDocumentsCommand.builder()
                .businessLicense(businessLicense)
                .mailOrderLicense(mailOrderLicense)
                .bankbookCopy(bankbookCopy)
                .foodManufactureLicense(foodManufactureLicense)
                .sellerId(sellerId)
                .build();
        }
    }

    @Schema(description = "계좌 정보 변경 요청 DTO")
    public record SellerAccountUpdateRequest(
        @Schema(description = "상점 ID", example = "1")
        @NotNull(message = "상점 ID는 필수입니다.")
        Long storeId,

        @Schema(description = "은행명", example = "국민은행")
        @NotBlank(message = "은행명은 필수입니다.")
        String bankName,

        @Schema(description = "예금주", example = "홍길동")
        @NotBlank(message = "예금주는 필수입니다.")
        String accountHolder,

        @Schema(description = "계좌번호", example = "123456-78-901234")
        @NotBlank(message = "계좌번호는 필수입니다.")
        String accountNumber
    ) {

    }

    @Schema(description = "판매자 정보 수정 요청 DTO")
    public record SellerUpdateRequest(
        @Schema(description = "상점 ID", example = "1")
        Long storeId,

        @Schema(description = "상점명", example = "빵그리의 오븐")
        String storeName,

        @Schema(description = "프로필 이미지 URL")
        String profile,

        @Schema(description = "한줄소개", example = "건강한 재료로 만드는 비건 베이커리")
        String shortDescription,

        @Schema(description = "대표 전화번호", example = "02-1234-5678")
        String phoneNumber,

        @Schema(description = "추가 연락처", example = "010-1234-5678")
        String subPhoneNumber,

        @Schema(description = "이메일 주소", example = "contact@bbangle.com")
        @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$",
            message = "이메일 형식이 유효하지 않습니다"
        )
        String email,

        @Schema(description = "기본 주소", example = "성남시 금광동 2322-31")
        String originAddress,

        @Schema(description = "상세 주소", example = "나동 456호")
        String originAddressDetail
    ) {

    }
}
