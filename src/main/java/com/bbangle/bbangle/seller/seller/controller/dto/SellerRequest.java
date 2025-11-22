package com.bbangle.bbangle.seller.seller.controller.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public class SellerRequest {

    public record SellerDocumentsRegisterRequest(
        @Schema(description = "사업자 등록증", requiredMode = REQUIRED, type = "string", format = "binary")
        MultipartFile businessLicense,

        @Schema(description = "통신판매업 신고증", requiredMode = REQUIRED, type = "string", format = "binary")
        MultipartFile mailOrderLicense,

        @Schema(description = "개인명의 통장 사본", requiredMode = REQUIRED, type = "string", format = "binary")
        MultipartFile bankbookCopy,

        @Schema(description = "즉석식품제조가공업 & 식품제조업", requiredMode = REQUIRED, type = "string", format = "binary")
        MultipartFile foodManufactureLicense
    ) {

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

    @Schema(description = "스토어명 변경 요청 DTO")
    public record SellerStoreNameUpdateRequest(
        @Schema(description = "상점 ID", example = "1")
        @NotNull(message = "상점 ID는 필수입니다.")
        Long storeId,

        @Schema(description = "변경할 스토어명", example = "빵그리의 새로운 오븐")
        @NotBlank(message = "스토어명은 필수입니다.")
        String storeName
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


    // TODO: v3
    public record SellerCreateRequest(
        @Schema(description = "스토어명", example = "빵그리의 오븐 1호점")
        @NotBlank(message = "스토어명은 필수입니다.")
        @Size(min = 3, max = 50, message = "스토어명은 3자 이상 50자 이하로 입력해주세요.") // 주석 반영
        String storeName,

        @Schema(description = "연락처", example = "01012345678")
        @NotBlank
        @Pattern(regexp = "^[0-9]{11}$", message = "연락처는 11자리 이하의 숫자만 입력 가능합니다.") // 주석 반영
        String phoneNumber,

        @Schema(description = "서브 연락처", example = "01012345678")
        @NotBlank
        @Pattern(regexp = "^[0-9]{11}$", message = "서브 연락처는 11자리 이하의 숫자만 입력 가능합니다.") // 주석 반영
        String subPhoneNumber,

        @Schema(description = "이메일", example = "user@example.com", format = "email")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @Schema(description = "판매자 주소", example = "(우편번호) 성남시 금광동 222-31")
        @NotBlank
        String originAddress,

        @Schema(description = "판매자 상세 주소", example = "나동 202호")
        @NotBlank
        @Size(max = 50, message = "상세 주소는 50자까지 입력 가능합니다.") // 주석 반영
        String originAddressDetail,

        @Schema(description = "중복검사 후 선택한 스토어의 아이디값", example = "1" )
        Long storeId
    ) {

        public SellerCreateCommand toCommand() {
            return SellerCreateCommand.builder()
                .storeName(storeName)
                .phoneNumber(phoneNumber)
                .subPhoneNumber(subPhoneNumber)
                .email(email)
                .originAddress(originAddress)
                .originAddressDetail(originAddressDetail)
                .storeId(storeId)
                .build();
        }
    }


}
