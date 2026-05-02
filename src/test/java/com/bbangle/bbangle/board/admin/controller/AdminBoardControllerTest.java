package com.bbangle.bbangle.board.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.board.admin.controller.dto.AdminProductResponse;
import com.bbangle.bbangle.board.admin.controller.dto.UploadApprovalResponse;
import com.bbangle.bbangle.board.admin.controller.dto.request.UploadApprovalDecisionRequest;
import com.bbangle.bbangle.board.admin.service.AdminBoardService;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.RejectionCategory;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.JsonDataEncoder;
import com.bbangle.bbangle.config.security.AdminApiPath;
import com.bbangle.bbangle.config.security.SecurityConfig;
import com.bbangle.bbangle.config.security.jwt.TestJwtPropertiesConfig;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.fixture.board.admin.controller.dto.AdminProductResponseFixture;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("[컨트롤러 테스트] AdminBoardController")
@Import({
    TestSlackAdaptorConfig.class,
    JsonDataEncoder.class,
    TokenProvider.class,
    TestJwtPropertiesConfig.class,
    ResponseService.class,
    SecurityConfig.class
})
@WebMvcTest(AdminBoardController.class)
@ActiveProfiles("test")
class AdminBoardControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AdminBoardService adminBoardService;

    @SpyBean
    private ResponseService responseService;

    @Autowired
    private JsonDataEncoder jsonDataEncoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("[상품 페이징 조회] 관리자는 상품 목록을 페이징 형태로 조회할 수 있다")
    void getAdminBoards_success() throws Exception {
        // given
        int page = 0;
        int size = 20;

        AdminProductResponse response = AdminProductResponseFixture.defaultResponse();
        Page<AdminProductResponse> pageResult = new PageImpl<>(List.of(response), PageRequest.of(page, size), 1);
        BbanglePageResponse<AdminProductResponse> bbanglePageResponse = BbanglePageResponse.of(pageResult);

        given(adminBoardService.getAdminBoards(any(Pageable.class))).willReturn(pageResult);

        // when & then
        mvc.perform(get(AdminApiPath.PREFIX + "/products")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.result.content.length()").value(1))
            .andExpect(jsonPath("$.result.page").value(page))
            .andExpect(jsonPath("$.result.size").value(size))
            .andExpect(jsonPath("$.result.totalPages").value(1))
            .andExpect(jsonPath("$.result.totalElements").value(1));
        then(adminBoardService).should().getAdminBoards(any(Pageable.class));
        then(responseService).should().getSingleResult(eq(bbanglePageResponse));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("[상품 페이징 조회] page가 범위를 초과하면 빈 목록을 반환한다")
    void getAdminBoards_emptyResult_whenPageOutOfRange() throws Exception {
        // given
        int page = 5;
        int size = 20;

        Page<AdminProductResponse> emptyPage =
            new PageImpl<>(
                List.of(),
                PageRequest.of(page, size),
                10 // 실제 데이터는 존재
            );

        BbanglePageResponse<AdminProductResponse> bbanglePageResponse =
            BbanglePageResponse.of(emptyPage);

        given(adminBoardService.getAdminBoards(any(Pageable.class)))
            .willReturn(emptyPage);

        // when & then
        mvc.perform(get(AdminApiPath.PREFIX + "/products")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.result.content.length()").value(0))
            .andExpect(jsonPath("$.result.page").value(page))
            .andExpect(jsonPath("$.result.size").value(size))
            .andExpect(jsonPath("$.result.totalPages").value(1))
            .andExpect(jsonPath("$.result.totalElements").value(10));

        then(adminBoardService).should().getAdminBoards(any(Pageable.class));
        then(responseService).should().getSingleResult(eq(bbanglePageResponse));
    }

    @Test
    @DisplayName("[상품 페이징 조회] ADMIN 권한이 없으면 접근에 실패한다")
    void getAdminBoards_fail_whenNoAdminRole() throws Exception {
        // when & then
        mvc.perform(get(AdminApiPath.PREFIX + "/products")
                .param("page", "0")
                .param("size", "20")
            )
            .andExpect(status().isUnauthorized());

        then(adminBoardService).shouldHaveNoInteractions();
        then(responseService).shouldHaveNoInteractions();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("[상품 삭제] 관리자는 상품을 soft delete 처리할 수 있다")
    void deleteBoards_success() throws Exception {
        // given
        List<Long> productIds = List.of(1L, 2L, 3L);

        willDoNothing().given(adminBoardService).deleteBoards(productIds);

        // when & then
        mvc.perform(delete(AdminApiPath.PREFIX + "/products")
                .param("productIds", "1", "2", "3")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.message").value("SUCCESS"));

        then(adminBoardService).should().deleteBoards(productIds);
        then(responseService).should().getSuccessResult();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("[업로드 승인 대기] 관리자는 업로드 대기 상품을 페이징 형태로 조회할 수 있다")
    void getUploadApprovals_success() throws Exception {
        // given
        int page = 0;
        int size = 20;

        Board board = BoardFixture.defaultBoard();
        UploadApprovalResponse response = new UploadApprovalResponse(
            board.getId(),
            board.getStore().getId(),
            board.getStore().getName(),
            board.getTitle()
        );
        Page<UploadApprovalResponse> pageResult =
            new PageImpl<>(List.of(response), PageRequest.of(page, size), 1);

        given(adminBoardService.getUploadApprovals(any(Pageable.class)))
            .willReturn(pageResult);

        // when & then
        mvc.perform(get(AdminApiPath.PREFIX + "/products/upload-approvals")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.result.content.length()").value(1))
            .andExpect(jsonPath("$.result.content[0].boardId").value(board.getId()))
            .andExpect(jsonPath("$.result.content[0].storeName").value(board.getStore().getName()))
            .andExpect(jsonPath("$.result.content[0].boardTitle").value(board.getTitle()))
            .andExpect(jsonPath("$.result.page").value(page))
            .andExpect(jsonPath("$.result.size").value(size))
            .andExpect(jsonPath("$.result.totalPages").value(1))
            .andExpect(jsonPath("$.result.totalElements").value(1));

        then(adminBoardService).should().getUploadApprovals(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("[업로드 승인 대기] 조회 결과가 없으면 빈 목록을 반환한다")
    void getUploadApprovals_emptyResult() throws Exception {
        // given
        int page = 0;
        int size = 20;

        Page<UploadApprovalResponse> emptyPage =
            new PageImpl<>(List.of(), PageRequest.of(page, size), 0);

        given(adminBoardService.getUploadApprovals(any(Pageable.class)))
            .willReturn(emptyPage);

        // when & then
        mvc.perform(get(AdminApiPath.PREFIX + "/products/upload-approvals")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.result.content.length()").value(0))
            .andExpect(jsonPath("$.result.page").value(page))
            .andExpect(jsonPath("$.result.size").value(size))
            .andExpect(jsonPath("$.result.totalPages").value(0))
            .andExpect(jsonPath("$.result.totalElements").value(0));

        then(adminBoardService).should().getUploadApprovals(any(Pageable.class));
    }

    @Test
    @DisplayName("[업로드 승인 대기] ADMIN 권한이 없으면 접근에 실패한다")
    void getUploadApprovals_fail_whenNoAdminRole() throws Exception {
        // when & then
        mvc.perform(get(AdminApiPath.PREFIX + "/products/upload-approvals")
                .param("page", "0")
                .param("size", "20")
            )
            .andExpect(status().isUnauthorized());

        then(adminBoardService).shouldHaveNoInteractions();
        then(responseService).shouldHaveNoInteractions();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("[업로드 승인/거절] 관리자는 상품을 승인할 수 있다")
    void decideUploadApproval_approve_success() throws Exception {
        // given
        Long boardId = 1L;
        UploadApprovalDecisionRequest req = new UploadApprovalDecisionRequest(DecisionType.APPROVE, null, null);

        willDoNothing().given(adminBoardService).decideUploadApproval(eq(boardId), any());

        // when & then
        mvc.perform(post(AdminApiPath.PREFIX + "/products/{boardId}/decision", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(req))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.message").value("SUCCESS"));

        then(adminBoardService).should().decideUploadApproval(eq(boardId), any());
        then(responseService).should().getSuccessResult();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("[업로드 승인/거절] 관리자는 상품을 거절할 수 있다")
    void decideUploadApproval_reject_success() throws Exception {
        // given
        Long boardId = 2L;
        UploadApprovalDecisionRequest req = new UploadApprovalDecisionRequest(DecisionType.REJECT,
            RejectionCategory.INAPPROPRIATE_BRAND_NAME, "브랜드명을 무단으로 사용하여 고객에게 혼동을 줄 수 있습니다.");
        willDoNothing().given(adminBoardService).decideUploadApproval(eq(boardId), any());

        // when & then
        mvc.perform(post(AdminApiPath.PREFIX + "/products/{boardId}/decision", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(req))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.message").value("SUCCESS"));

        then(adminBoardService).should().decideUploadApproval(eq(boardId), any());
        then(responseService).should().getSuccessResult();
    }

    @Test
    @DisplayName("[업로드 승인/거절] ADMIN 권한이 없으면 접근에 실패한다")
    void decideUploadApproval_fail_whenNoAdminRole() throws Exception {
        // given
        Long boardId = 1L;
        UploadApprovalDecisionRequest req = new UploadApprovalDecisionRequest(DecisionType.APPROVE, null, null);

        // when & then
        mvc.perform(post(AdminApiPath.PREFIX + "/products/{boardId}/decision", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonDataEncoder.encode(req))
            )
            .andExpect(status().isUnauthorized());

        then(adminBoardService).shouldHaveNoInteractions();
        then(responseService).shouldHaveNoInteractions();
    }

}
