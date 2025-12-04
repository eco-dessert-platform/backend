package com.bbangle.bbangle.admin.admin.controller;

import com.bbangle.bbangle.admin.admin.controller.swagger.AdminAuthApi;
import com.bbangle.bbangle.admin.admin.dto.AdminLoginResponse;
import com.bbangle.bbangle.admin.admin.dto.AdminRequest.AdminLoginRequest;
import com.bbangle.bbangle.admin.admin.service.AdminAuthService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminAuthController implements AdminAuthApi {

    private final AdminAuthService adminAuthService;
    private final ResponseService responseService;

    @Override
    @PostMapping("/login")
    public SingleResult<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        return responseService.getSingleResult(adminAuthService.login(request));
    }

    @Override
    @PostMapping("/logout")
    public CommonResult logout(@AuthenticationPrincipal Long adminId) {
        adminAuthService.logout(adminId);
        return responseService.getSuccessResult();
    }
}
