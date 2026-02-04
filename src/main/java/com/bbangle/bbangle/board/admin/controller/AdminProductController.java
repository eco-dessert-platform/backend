package com.bbangle.bbangle.board.admin.controller;

import com.bbangle.bbangle.board.admin.controller.dto.AdminEditStockRequest;
import com.bbangle.bbangle.board.admin.controller.swagger.AdminProductApi;
import com.bbangle.bbangle.board.admin.service.AdminProductService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.AdminApiPath;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping(AdminApiPath.PREFIX + "/options")
@RestController
public class AdminProductController implements AdminProductApi {
    private final AdminProductService adminProductService;
    private final ResponseService responseService;

    @Override
    @PatchMapping("{optionId}/stock")
    public CommonResult editProductOptionStock(
        @PathVariable Long optionId,
        @RequestBody @Valid AdminEditStockRequest adminEditStockRequest
    ) {
        adminProductService.editProductStock(
            optionId,
            adminEditStockRequest.amount(),
            adminEditStockRequest.editStockFlag()
        );
        return responseService.getSuccessResult();
    }
}
