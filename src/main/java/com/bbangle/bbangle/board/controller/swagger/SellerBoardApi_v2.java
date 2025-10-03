package com.bbangle.bbangle.board.controller.swagger;

import com.bbangle.bbangle.board.dto.BoardUploadRequest_v2;
import com.bbangle.bbangle.common.dto.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Seller Boards", description = "판매자 게시판 API")
public interface SellerBoardApi_v2 {

    @Operation(summary = "(판매자) 게시글 등록")
    CommonResult upload(
        @Parameter(description = "스토어 ID") @PathVariable Long storeId,
        @RequestBody BoardUploadRequest_v2 request
    );
}
