package com.bbangle.bbangle.seller.admin.controller.swagger;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerDocumentDownloadRequest;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Tag(name = "Admin Store", description = "(관리자) 판매자 관리 API")
public interface AdminSellerApi {

    @Operation(
        summary = "(관리자) 판매자 회원가입 및 스토어 등록 요청 목록 조회",
        description = "승인 대기 중인 요청 목록을 조회합니다."
    )
    SingleResult<AdminSellerResponse.AdminSellerApplicationList> getSellerApplicationList(
        @Parameter(description = "조회할 페이지 번호", example = "1")
        @RequestParam(defaultValue = "1")
        @Min(1)
        int page
    );

    @Operation(
        summary = "(관리자) 판매자 서류 ZIP 다운로드",
        description = "선택된 판매자들의 서류를 스토어명 디렉토리 구조로 ZIP 파일로 다운로드합니다."
    )
    ResponseEntity<StreamingResponseBody> downloadSellerDocuments(
        @RequestBody @Valid AdminSellerDocumentDownloadRequest request
    );
}
