package com.bbangle.bbangle.store.board.controller.swagger;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.exception.GlobalControllerAdvice;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "ProductBoards", description = "상품 게시글 관련 API")
public interface ProductBoardApi_v3 {


    @Operation(
        summary = "상품 게시글 복제",
        description = "상품 게시글을 복제 합니다."
    )

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "상품 게시글 복제",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @ExampleObject(
                    name = "successResponse",
                    summary = "성공응답 예시",
                    value = """
                        {
                            "success": true,
                            "code": 0,
                            "message": "SUCCESS",
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(
                schema = @Schema(implementation = GlobalControllerAdvice.class)
            )
        )
    })
    CommonResult copyProductBoard(
        @Parameter(name = "storeId", description = "스토어 ID", example = "1")
        Long storeId,
        @Parameter(name = "boardId", description = "게시글 ID", example = "1")
        Long boardId);

}
