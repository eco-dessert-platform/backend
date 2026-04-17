package com.bbangle.bbangle.seller.admin.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.AdminApiPath;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerRequest;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationList;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationRejectList;
import com.bbangle.bbangle.seller.admin.controller.swagger.AdminSellerApi;
import com.bbangle.bbangle.seller.admin.facade.AdminSellerFacade;
import com.bbangle.bbangle.seller.admin.service.AdminSellerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(AdminApiPath.PREFIX + "/sellers")
public class AdminSellerController implements AdminSellerApi {

    private final ResponseService responseService;
    private final AdminSellerFacade adminSellerFacade;
    private final AdminSellerService adminSellerService;

    @Override
    @GetMapping()
    public SingleResult<AdminSellerApplicationList> getSellerApplicationList(
        @RequestParam(defaultValue = "1") @Min(1) int page
    ) {
        return responseService.getSingleResult(
            adminSellerFacade.getAdminSellerApplicationList(page)
        );
    }

    @Override
    @PatchMapping("/reject")
    public SingleResult<AdminSellerApplicationRejectList> rejectSellerApplications(
        @Valid @RequestBody AdminSellerRequest.StoreApplicationIds request
    ) {
        return responseService.getSingleResult(
            adminSellerService.rejectStoreApplications(request.applicationIds())
        );
    }
}
