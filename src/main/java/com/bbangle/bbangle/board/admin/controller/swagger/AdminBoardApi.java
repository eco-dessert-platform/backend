package com.bbangle.bbangle.board.admin.controller.swagger;

import com.bbangle.bbangle.board.admin.controller.dto.AdminProductResponse;
import com.bbangle.bbangle.board.admin.controller.dto.AdminRemoveProductRequest;
import com.bbangle.bbangle.board.admin.controller.dto.UploadApprovalResponse;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "AdminProduct", description = "(관리자) 상품 게시글 관련 API")
public interface AdminBoardApi {

    @Operation(
        summary = "관리자 상품 목록 조회",
        description = "관리자 페이지에서 상품 목록을 페이징 형태로 조회합니다."
    )
    SingleResult<BbanglePageResponse<AdminProductResponse>> getAdminBoards(
        @ParameterObject Pageable pageable
    );

    @Operation(
        summary = "관리자 상품 리스트 삭제",
        description = "관리자가 선택한 상품 목록을 삭제합니다."
    )
    CommonResult deleteBoards(
        @Parameter(description = "삭제할 상품 ID 목록", example = "[1, 2]")
        List<Long> productIds
    );

    @Operation(
        summary = "관리자 상품 옵션 삭제",
        description = "관리자가 선택한 상품의 옵션을 삭제합니다. "
    )
    CommonResult deleteProducts(
        @Parameter(description = "옵션을 삭제할 상품ID", example = "1")
        Long productId,
        @Valid AdminRemoveProductRequest request
    );

    @Operation(
        summary = "업로드 상품 승인 대기 목록 조회",
        description = "판매자가 업로드한 승인 대기(PENDING) 상태의 상품 목록을 페이지네이션으로 조회합니다."
    )
    SingleResult<BbanglePageResponse<UploadApprovalResponse>> getUploadApprovals(
        @ParameterObject Pageable pageable
    );
    
}
