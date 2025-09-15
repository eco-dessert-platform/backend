package com.bbangle.bbangle.seller.controller.swagger;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.exception.ErrorResponse;
import com.bbangle.bbangle.seller.controller.SellerRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Sellers", description = "판매자 관련 API")
@RequestMapping("/api/v1/sellers")
public interface SellerApi {

    // TODO : 우선적으로 성공응답만 명시적으로 지정
    @Operation(summary = "신규 판매자 생성", description = "새로운 판매자 정보를 시스템에 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200",
            description = "판매자 생성 성공",
            content = @Content(
                schema = @Schema(implementation = CommonResult.class),
                examples = @ExampleObject(
                    name = "successResponse",
                    summary = "성공응답 예시",
                    value = """
                        {
                            "success" : true,
                            "code" : 0,
                            "message" : "SUCCESS"
                        } 
                        """
                ))),
        @ApiResponse(responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            ))
    })
    @PostMapping
    CommonResult createSeller(@RequestBody SellerRequest.sellerCreateRequest request);

}
