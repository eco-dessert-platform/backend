package com.bbangle.bbangle.seller.seller.controller.swagger;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.exception.GlobalControllerAdvice;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.AccountVerificationRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerAccountUpdateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerCreateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerDocumentsRegisterRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerStoreNameUpdateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Seller", description = "(판매자) 판매자 API")
public interface SellerApi {

    @Operation(
        summary = "(판매자) 판매자 서류 등록",
        description = """
            판매자 입점에 필요한 서류를 등록합니다.
            - 사업자 등록증
            - 통신판매업 신고증
            - 개인명의 통장 사본
            - 즉석식품제조가공업 & 식품제조업 등록증
            - 계좌인증 ID
            
            모든 서류는 필수이며, 이미지(jpg, jpeg, png) 또는 PDF 형식으로 업로드 가능합니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "서류 등록 성공",
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
                            "list": [
                                {
                                    "sellerDocumentId": 1,
                                    "sellerId": 1,
                                    "url": "https://s3.amazonaws.com/bbangle/documents/business_license.pdf",
                                    "documentName": "business_license.pdf",
                                    "documentType": "BUSINESS_REGISTRATION_CERTIFICATE",
                                    "status": "PENDING",
                                    "createdAt": "2024-01-14T10:30:00"
                                },
                                {
                                    "sellerDocumentId": 2,
                                    "sellerId": 1,
                                    "url": "https://s3.amazonaws.com/bbangle/documents/mail_order.pdf",
                                    "documentName": "mail_order.pdf",
                                    "documentType": "MAIL_ORDER_SALES_REPORT",
                                    "status": "PENDING",
                                    "createdAt": "2024-01-14T10:30:00"
                                }
                            ]
                        }
                        """
                )
            )
        )
    })
    CommonResult registerDocuments(
        SellerDocumentsRegisterRequest request,
        Long sellerId
    );

    // TODO: v2
    @Operation(
        summary = "판매자 정보 수정",
        description = "기존 판매자 정보를 전체 수정합니다."
    )
    CommonResult updateSeller(
        @RequestBody SellerUpdateRequest request,
        @AuthenticationPrincipal Long sellerId
    );

    @Operation(
        summary = "스토어명 변경",
        description = "스토어명을 변경합니다. (최초 1회만 가능)"
    )
    CommonResult updateStoreName(
        @RequestBody SellerStoreNameUpdateRequest request,
        @AuthenticationPrincipal Long sellerId
    );

    @Operation(
        summary = "계좌 정보 변경",
        description = "판매자 계좌 정보를 변경합니다."
    )
    CommonResult updateAccount(
        @RequestBody SellerAccountUpdateRequest request,
        @AuthenticationPrincipal Long sellerId
    );


    // TODO : v3
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
                schema = @Schema(implementation = GlobalControllerAdvice.class)
            )
        )
    })
    CommonResult createSeller(
        SellerCreateRequest request,
        MultipartFile profileImage);

    @Operation(
        summary = "계좌 인증",
        description = """
            판매자의 계좌를 인증합니다.
            - 토스페이먼츠 표준 은행코드를 사용합니다.
            - PG 계약 이슈로 API가 변경될 수 있습니다.
            """
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
                            "success": true,
                            "code": 0,
                            "message": "SUCCESS",
                            "result": {
                                "id": 1,
                                "sellerId": 1,
                                "verified": true,
                                "createdAt": "2025-12-10T14:32:10"
                            }
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
    CommonResult accountVerification(
        @RequestBody AccountVerificationRequest request,
        @AuthenticationPrincipal Long sellerId);

}
