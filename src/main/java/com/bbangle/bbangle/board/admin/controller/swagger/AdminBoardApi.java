package com.bbangle.bbangle.board.admin.controller.swagger;

import com.bbangle.bbangle.board.admin.controller.dto.AdminProductResponse;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
}
