package com.bbangle.bbangle.claim.seller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.seller.controller.dto.CancelDecisionRequest;
import com.bbangle.bbangle.claim.seller.service.SellerClaimService;
import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("[컨트롤러 테스트] SellerCancelController")
@WebMvcTest(controllers = SellerCancelController.class)
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@ActiveProfiles("test")
class SellerCancelControllerTest {

    private static final String BASE_URL = SellerApiPath.PREFIX + "/cancels";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JsonDataEncoder jsonDataEncoder;

    @MockBean
    private SellerClaimService sellerClaimService;

    @Nested
    @DisplayName("POST /api/v1/seller/cancels/{cancelId}/decision")
    class CancelDecision {

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("APPROVE 요청이 정상적으로 처리되면 200 OK와 success=true를 반환한다")
        void given_approveRequest_when_decision_then_success() throws Exception {
            // given
            Long cancelId = 1L;
            CancelDecisionRequest request = new CancelDecisionRequest(DecisionType.APPROVE, "취소 승인");

            willDoNothing().given(sellerClaimService)
                .decision(any(), any(), any(DecisionType.class), any());

            // when & then
            mvc.perform(
                    post(BASE_URL + "/{cancelId}/decision", cancelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDataEncoder.encode(request))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("SUCCESS"));

            then(sellerClaimService).should()
                .decision(eq(cancelId), isNull(), eq(DecisionType.APPROVE), eq("취소 승인"));
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("REJECT 요청이 정상적으로 처리되면 200 OK와 success=true를 반환한다")
        void given_rejectRequest_when_decision_then_success() throws Exception {
            // given
            Long cancelId = 1L;
            CancelDecisionRequest request = new CancelDecisionRequest(DecisionType.REJECT, "취소 거절 사유");

            willDoNothing().given(sellerClaimService)
                .decision(any(), any(), any(DecisionType.class), any());

            // when & then
            mvc.perform(
                    post(BASE_URL + "/{cancelId}/decision", cancelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDataEncoder.encode(request))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("SUCCESS"));

            then(sellerClaimService).should()
                .decision(eq(cancelId), isNull(), eq(DecisionType.REJECT), eq("취소 거절 사유"));
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("decisionType이 null이면 400 Bad Request를 반환한다")
        void given_nullDecisionType_when_decision_then_badRequest() throws Exception {
            // given
            Long cancelId = 1L;
            CancelDecisionRequest request = new CancelDecisionRequest(null, "사유");

            // when & then
            mvc.perform(
                    post(BASE_URL + "/{cancelId}/decision", cancelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDataEncoder.encode(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("존재하지 않는 claim이면 400 Bad Request와 에러코드 -771을 반환한다")
        void given_nonExistentClaim_when_decision_then_claimNotFound() throws Exception {
            // given
            Long cancelId = 999L;
            CancelDecisionRequest request = new CancelDecisionRequest(DecisionType.APPROVE, "승인");

            willThrow(new BbangleException(BbangleErrorCode.CLAIM_NOT_FOUND))
                .given(sellerClaimService)
                .decision(any(), any(), any(DecisionType.class), any());

            // when & then
            mvc.perform(
                    post(BASE_URL + "/{cancelId}/decision", cancelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDataEncoder.encode(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-771))
                .andExpect(jsonPath("$.message").value(BbangleErrorCode.CLAIM_NOT_FOUND.getMessage()));
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("판매자와 claim이 일치하지 않으면 401 Unauthorized와 에러코드 -772를 반환한다")
        void given_mismatchedSeller_when_decision_then_unauthorized() throws Exception {
            // given
            Long cancelId = 1L;
            CancelDecisionRequest request = new CancelDecisionRequest(DecisionType.APPROVE, "승인");

            willThrow(new BbangleException(BbangleErrorCode.SELLER_CLAIM_MISMATCH))
                .given(sellerClaimService)
                .decision(any(), any(), any(DecisionType.class), any());

            // when & then
            mvc.perform(
                    post(BASE_URL + "/{cancelId}/decision", cancelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDataEncoder.encode(request))
                )
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-772))
                .andExpect(jsonPath("$.message").value(BbangleErrorCode.SELLER_CLAIM_MISMATCH.getMessage()));
        }

        @Test
        @WithMockUser(roles = "SELLER")
        @DisplayName("이미 처리된 claim이면 400 Bad Request와 에러코드 -773을 반환한다")
        void given_alreadyProcessedClaim_when_decision_then_invalidStatus() throws Exception {
            // given
            Long cancelId = 1L;
            CancelDecisionRequest request = new CancelDecisionRequest(DecisionType.APPROVE, "승인");

            willThrow(new BbangleException(BbangleErrorCode.CLAIM_INVALID_STATUS))
                .given(sellerClaimService)
                .decision(any(), any(), any(DecisionType.class), any());

            // when & then
            mvc.perform(
                    post(BASE_URL + "/{cancelId}/decision", cancelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDataEncoder.encode(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(-773))
                .andExpect(jsonPath("$.message").value(BbangleErrorCode.CLAIM_INVALID_STATUS.getMessage()));
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 요청하면 401 Unauthorized를 반환한다")
        void given_unauthenticatedUser_when_decision_then_unauthorized() throws Exception {
            // given
            Long cancelId = 1L;
            CancelDecisionRequest request = new CancelDecisionRequest(DecisionType.APPROVE, "승인");

            // when & then
            mvc.perform(
                    post(BASE_URL + "/{cancelId}/decision", cancelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDataEncoder.encode(request))
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser("ADMIN")
        @DisplayName("SELLER 권한이 아닌 사용자가 요청하면 403 Forbidden을 반환한다")
        void given_nonSellerUser_when_decision_then_forbidden() throws Exception {
            // given
            Long cancelId = 1L;
            CancelDecisionRequest request = new CancelDecisionRequest(DecisionType.APPROVE, "승인");

            // when & then
            mvc.perform(
                    post(BASE_URL + "/{cancelId}/decision", cancelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDataEncoder.encode(request))
                )
                .andDo(print())
                .andExpect(status().isForbidden());
        }
    }
}
