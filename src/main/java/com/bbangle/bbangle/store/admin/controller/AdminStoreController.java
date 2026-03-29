package com.bbangle.bbangle.store.admin.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.AdminApiPath;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.admin.controller.swagger.AdminStoreApi;
import com.bbangle.bbangle.store.admin.service.AdminStoreService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(AdminApiPath.PREFIX + "/stores")
@Validated
public class AdminStoreController implements AdminStoreApi {

    private final ResponseService responseService;
    private final AdminStoreService adminStoreService;

    @Override
    @GetMapping()
    public SingleResult<UpdateStoreNameRequest> getUpdateStoreNames(
        @RequestParam(defaultValue = "1")
        @Min(1)
        int page
    ) {
        return responseService.getSingleResult(
            adminStoreService.getPendingRequests(page)
        );
    }
}