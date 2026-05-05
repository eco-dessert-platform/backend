package com.bbangle.bbangle.seller.admin.controller.swagger;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerRequest;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerRequest.StoreApplicationApprove;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Admin Seller", description = "(관리자) 판매자 관리 API")
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
        summary = "(관리자) 판매자 회원 가입 및 스토어 등록 요청 목록 승인",
        description = "판매자의 회원 가입 및 스토어 등록 요청을 승인합니다."
    )
    SingleResult<AdminSellerResponse.AdminSellerApplicationApproveList> approveSellerApplications(
        @Valid @RequestBody List<StoreApplicationApprove> requests
    );

    @Operation(
        summary = "(관리자) 판매자 스토어 등록 요청 목록 거절",
        description = "판매자의 스토어 등록 요청을 거절합니다."
    )
    SingleResult<AdminSellerResponse.AdminSellerApplicationRejectList> rejectSellerApplications(
        @Valid @RequestBody AdminSellerRequest.StoreApplicationIds request
    );
}
