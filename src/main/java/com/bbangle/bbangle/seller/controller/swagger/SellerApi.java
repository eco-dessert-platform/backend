package com.bbangle.bbangle.seller.controller.swagger;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.exception.ErrorResponse;
import com.bbangle.bbangle.seller.controller.SellerRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Sellers", description = "판매자 관련 API")
@RequestMapping("/api/v1/sellers")
public interface SellerApi {


    @Operation(
        summary = "신규 판매자 생성",
        description = "판매자 정보(JSON)와 프로필 이미지 파일을 업로드합니다."
    )

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
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
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping(
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    CommonResult createSeller(
        @Parameter(
            description = "판매자 생성 요청",
            schema = @Schema(implementation = SellerCreateMultipartDoc.class) // <-- Swagger 문서에는 이걸 노출
        )
        @Valid @ModelAttribute SellerRequest.SellerCreateRequest request);


    @Operation(
        summary = "계좌 인증",
        description = "판매자의 계좌를 인증합니다, 외부 Open API spec에 따라 변경가능성이 있습니다."
    )

    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "계좌 인증 성공",
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
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터",
            content = @Content(
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/account")
    CommonResult accountVerification(@RequestParam String accountNumber,
        @RequestParam String sellerName);


}
