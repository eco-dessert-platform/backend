package com.bbangle.bbangle.admin.admin.controller.swagger;

import com.bbangle.bbangle.admin.admin.dto.AdminLoginResponse;
import com.bbangle.bbangle.admin.admin.dto.AdminRequest.AdminLoginRequest;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin", description = "관리자 API")
public interface AdminApi {

    @Operation(summary = "관리자 로그인", description = "관리자 계정으로 로그인합니다.")
    SingleResult<AdminLoginResponse> login(
        AdminLoginRequest request
    );

    @Operation(summary = "관리자 로그아웃", description = "관리자 계정 로그아웃")
    CommonResult logout(
        @Parameter(hidden = true) Long adminId
    );
}
