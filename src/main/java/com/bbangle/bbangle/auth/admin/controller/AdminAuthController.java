package com.bbangle.bbangle.auth.admin.controller;

import com.bbangle.bbangle.auth.admin.controller.swagger.AdminAuthApi;
import com.bbangle.bbangle.auth.admin.dto.AdminLoginResponse;
import com.bbangle.bbangle.auth.admin.dto.AdminRequest.AdminLoginRequest;
import com.bbangle.bbangle.auth.admin.service.AdminAuthService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.AdminApiPath;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(AdminApiPath.PREFIX)
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
