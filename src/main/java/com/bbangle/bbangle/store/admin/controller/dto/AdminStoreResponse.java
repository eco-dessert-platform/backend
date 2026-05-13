package com.bbangle.bbangle.store.admin.controller.dto;

import com.bbangle.bbangle.store.admin.service.model.UpdateStoreNamesInfo.UpdateStoreNames;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.domain.model.StoreNameRejectCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

public class AdminStoreResponse {

    @Builder
    @Schema(description = "판매자 스토어명 변경 요청 목록 DTO")
    public record UpdateStoreNameRequest(
        List<UpdateStoreNames> updateStoreNames,
        @Schema(description = "전체 데이터 갯수", example = "1") long totalElements,
        @Schema(description = "전체 페이지", example = "1") int totalPages,
        @Schema(description = "이전 페이지 여부", example = "false") boolean hasPrevious,
        @Schema(description = "다음 페이지 여부", example = "false") boolean hasNext
    ) {}

    @Builder
    @Schema(description = "판매자 스토어명 변경 승인 결과 DTO")
    public record UpdateStoreNameApprove(
        @Schema(description = "스토어 Id", example = "1") long storeId,
        @Schema(description = "이전 스토어 이름", example = "빵그리") String prevName,
        @Schema(description = "변경된 스토어 이름", example = "빵그리의 오븐") String updateName,
        @Schema(description = "요청 승인 상태", example = "APPROVE") StoreApprovalStatus status,
        @Schema(description = "수정일", example = "2026-01-01T01:23:45") LocalDateTime modifiedAt
    ) {}

    @Builder
    @Schema(description = "판매자 스토어명 변경 거부 결과 DTO")
    public record UpdateStoreNameReject(
        @Schema(description = "판매자 스토어명 변경 요청 Id", example = "1") Long requestId,
        @Schema(description = "스토어 id", example = "1") Long storeId,
        @Schema(description = "현재 스토어 이름", example = "빵그리") String currentName,
        @Schema(description = "변경할 스토어 이름", example = "빵그리의 오븐") String newName,
        @Schema(description = "요청 승인 상태", example = "REJECT") StoreApprovalStatus status,
        @Schema(description = "요청 거부 종류", example = "ETC") StoreNameRejectCategory category,
        @Schema(description = "요청 거부 상세 사유", example = "부적절한 이름") String rejectDetail
    ) {}

    @Builder
    @Schema(description = "스토어 상세 정보 DTO")
    public record StoreDetailResponse(
        @Schema(description = "스토어 ID", example = "1") Long storeId,
        @Schema(description = "스토어명", example = "빵그리의 오븐") String name,
        @Schema(description = "사업자 번호", example = "12345") String identifier,
        @Schema(description = "스토어 한 줄 소개", example = "건강한 디저트를 만드는 베이커리") String introduce,
        @Schema(description = "스토어 프로필 이미지 URL", example = "https://d37g3q9mfan3cw.cloudfront.net/store/000000/logo.png") String profile,
        @Schema(description = "스토어 연락처", example = "01012345678") String phoneNumber,
        @Schema(description = "스토어 추가 연락처", example = "01012345678") String subPhoneNumber,
        @Schema(description = "스토어 이메일", example = "user@example.com") String email,
        @Schema(description = "스토어 출고지 주소", example = "(우편번호) 성남시 금광동 222-31") String originAddress,
        @Schema(description = "스토어 출고지 상세 주소", example = "나동 202호") String originAddressDetail
    ) {}
}
