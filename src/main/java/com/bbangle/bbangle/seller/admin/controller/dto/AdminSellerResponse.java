package com.bbangle.bbangle.seller.admin.controller.dto;

import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

public class AdminSellerResponse {

    @Builder
    @Schema(description = "승인 및 거절 처리 실패한 신청 상세 정보")
    public record FailDetail(
        @Schema(description = "승인 및 거절 실패한 신청 Id", example = "1") long storeApplicationId,
        @Schema(description = "승인 및 거절 실패한 상세 사유", example = "존재하지 않는 신청입니다.") String reason
    ) {}

    @Builder
    @Schema(description = "판매자 신규 회원가입 정보 DTO")
    public record AdminSellerApplication(
        @Schema(description = "스토어 신청서 ID", example = "1")
        Long storeApplicationId,
        SellerStoreDTO sellerStoreDTO,
        SellerDTO sellerDTO
    ) {
        @Builder
        @Schema(description = "판매자 스토어 등록 신청 정보 DTO")
        public record SellerStoreDTO(
            @Schema(description = "스토어명", example = "빵그리의 오븐") String storeName,
            @Schema(description = "스토어 연락처", example = "01012345678") String phone,
            @Schema(description = "스토어 추가 연락처", example = "01012345678") String subPhone,
            @Schema(description = "스토어 이메일", example = "user@example.com") String email,
            @Schema(description = "스토어 출고지 주소", example = "(우편번호) 성남시 금광동 222-31") String originAddressLine,
            @Schema(description = "스토어 출고지 상세 주소", example = "나동 202호") String originAddressDetail
        ) {}

        @Builder
        @Schema(description = "판매자 정보 DTO")
        public record SellerDTO(
            @Schema(description = "판매자 ID", example = "1") Long sellerId,
            @Schema(description = "은행 코드", example = "92") String bankCode,
            @Schema(description = "예금주", example = "빵그리") String accountHolder,
            @Schema(description = "계좌 번호", example = "111-1111") String accountNumber,
            @Schema(description = "판매자 계정 생성일", example = "2026-01-01T01:23:45") LocalDateTime createdAt
        ) {}
    }

    @Builder
    @Schema(description = "판매자 신규 회원가입 정보 목록 DTO")
    public record AdminSellerApplicationList(
        List<AdminSellerApplication> adminSellerApplicationList,
        @Schema(description = "전체 데이터 갯수", example = "1") long totalElements,
        @Schema(description = "전체 페이지", example = "1") int totalPages,
        @Schema(description = "이전 페이지 여부", example = "false") boolean hasPrevious,
        @Schema(description = "다음 페이지 여부", example = "false") boolean hasNext
    ) {}

    @Builder
    @Schema(description = "판매자 스토어 등록 신청 거절 결과 목록 DTO")
    public record AdminSellerApplicationRejectList(
        @Schema(description = "거절 처리한 신청 Id 목록", example = "[1]") List<Long> successIds,
        List<FailDetail> failDetails
    ) {}

    @Builder
    @Schema(description = "판매자 스토어 등록 신청 승인 결과 목록 DTO")
    public record AdminSellerApplicationApproveList(
        List<SuccessDetail> successDetails,
        List<FailDetail> failDetails
    ) {

        @Builder
        @Schema(description = "승인 성공한 신청 상세 정보")
        public record SuccessDetail(
            @Schema(description = "스토어 신청서 ID", example = "1") Long storeApplicationId,
            @Schema(description = "스토어 신청서 상태", example = "APPROVE") StoreApprovalStatus storeApplicationStatus,
            StoreDTO storeDTO,
            SellerDTO sellerDTO
        ) {

            @Builder
            @Schema(description = "스토어 정보 DTO")
            public record StoreDTO(
                @Schema(description = "스토어 Id", example = "1") long storeId,
                @Schema(description = "스토어명", example = "빵그리의 오븐") String storeName,
                @Schema(description = "스토어 한 줄 소개", example = "빵그리의 오븐입니다.") String introduce,
                @Schema(description = "스토어 프로필", example = "test@example.com") String profile,
                @Schema(description = "스토어 연락처", example = "01012345678") String phone,
                @Schema(description = "스토어 추가 연락처", example = "01012345678") String subPhone,
                @Schema(description = "스토어 이메일", example = "user@example.com") String email,
                @Schema(description = "스토어 출고지 주소", example = "(우편번호) 성남시 금광동 222-31") String originAddressLine,
                @Schema(description = "스토어 출고지 상세 주소", example = "나동 202호") String originAddressDetail
            ) {}

            @Builder
            @Schema(description = "판매자 정보 DTO")
            public record SellerDTO(
                @Schema(description = "판매자 ID", example = "1") Long sellerId,
                @Schema(description = "판매자 이름", example = "빵그리") String sellerName,
                @Schema(description = "판매자 상태", example = "APPROVED") CertificationStatus sellerStatus
            ) {}
        }
    }
}
