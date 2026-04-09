package com.bbangle.bbangle.board.admin.controller.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.board.domain.RejectionCategory;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] UploadApprovalDecisionRequest DTO 검증")
class UploadApprovalDecisionRequestTest {

    @Nested
    @DisplayName("APPROVE 선택 시")
    class ApproveDecision {

        @Test
        @DisplayName("rejectCategory와 rejectReason이 null이어도 검증을 통과한다")
        void approveWithNullRejectFields() {
            // given & when
            UploadApprovalDecisionRequest request = new UploadApprovalDecisionRequest(
                DecisionType.APPROVE,
                null,
                null
            );

            // then
            assertThat(request.isValidRejectCategory()).isTrue();
            assertThat(request.isValidRejectReason()).isTrue();
        }

        @Test
        @DisplayName("rejectCategory와 rejectReason이 있어도 검증을 통과한다")
        void approveIgnoresRejectFields() {
            // given & when
            UploadApprovalDecisionRequest request = new UploadApprovalDecisionRequest(
                DecisionType.APPROVE,
                RejectionCategory.INAPPROPRIATE_BRAND_NAME,
                "상관없음"
            );

            // then
            assertThat(request.isValidRejectCategory()).isTrue();
            assertThat(request.isValidRejectReason()).isTrue();
        }
    }

    @Nested
    @DisplayName("REJECT 선택 시")
    class RejectDecision {

        @Test
        @DisplayName("rejectCategory만 있고 rejectReason이 null이면 검증을 통과한다")
        void rejectSuccessWithoutReason() {
            // given & when
            UploadApprovalDecisionRequest request = new UploadApprovalDecisionRequest(
                DecisionType.REJECT,
                RejectionCategory.INAPPROPRIATE_BRAND_NAME,
                null
            );

            // then
            assertThat(request.isValidRejectCategory()).isTrue();
            assertThat(request.isValidRejectReason()).isTrue();
        }

        @Test
        @DisplayName("rejectCategory와 rejectReason 모두 있으면 검증을 통과한다")
        void rejectSuccessWithReason() {
            // given & when
            UploadApprovalDecisionRequest request = new UploadApprovalDecisionRequest(
                DecisionType.REJECT,
                RejectionCategory.INAPPROPRIATE_BRAND_NAME,
                "브랜드명을 무단으로 사용하였습니다."
            );

            // then
            assertThat(request.isValidRejectCategory()).isTrue();
            assertThat(request.isValidRejectReason()).isTrue();
        }

        @Test
        @DisplayName("rejectCategory가 null이면 검증 실패한다")
        void rejectFailWhenCategoryIsNull() {
            // given & when
            UploadApprovalDecisionRequest request = new UploadApprovalDecisionRequest(
                DecisionType.REJECT,
                null,
                "거절 사유"
            );

            // then
            assertThat(request.isValidRejectCategory()).isFalse();
        }

        @Test
        @DisplayName("rejectReason 길이가 500자를 초과하면 검증 실패한다")
        void rejectFailWhenReasonExceeds500Chars() {
            // given
            String longReason = "a".repeat(501);
            UploadApprovalDecisionRequest request = new UploadApprovalDecisionRequest(
                DecisionType.REJECT,
                RejectionCategory.INAPPROPRIATE_BRAND_NAME,
                longReason
            );

            // then
            assertThat(request.isValidRejectReason()).isFalse();
        }

        @Test
        @DisplayName("rejectReason 길이가 정확히 500자이면 검증을 통과한다")
        void rejectSuccessWhenReasonExactly500Chars() {
            // given
            String reason = "a".repeat(500);
            UploadApprovalDecisionRequest request = new UploadApprovalDecisionRequest(
                DecisionType.REJECT,
                RejectionCategory.INAPPROPRIATE_BRAND_NAME,
                reason
            );

            // then
            assertThat(request.isValidRejectReason()).isTrue();
        }

        @Test
        @DisplayName("rejectReason이 공백이면 검증을 통과한다 (길이만 체크)")
        void rejectSuccessWhenReasonIsBlank() {
            // given & when
            UploadApprovalDecisionRequest request = new UploadApprovalDecisionRequest(
                DecisionType.REJECT,
                RejectionCategory.INAPPROPRIATE_BRAND_NAME,
                "   "
            );

            // then
            assertThat(request.isValidRejectReason()).isTrue();
        }
    }
}
