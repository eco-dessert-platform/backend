package com.bbangle.bbangle.store.admin.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.AdminApiPath;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest.UpdateStoreNameRejectRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.StoreSearchResult;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameApprove;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameReject;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.admin.controller.swagger.AdminStoreApi;
import com.bbangle.bbangle.store.admin.service.AdminStoreService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
    @GetMapping("/search")
    public SingleResult<StoreSearchResult> searchStores(
        @RequestParam(required = false) String storeName,
        @RequestParam(defaultValue = "1") @Min(1) int page
    ) {
        return responseService.getSingleResult(
            adminStoreService.searchStoresByName(storeName, page)
        );
    }

    @Override
    @GetMapping()
    public SingleResult<UpdateStoreNameRequest> getUpdateStoreNames(
        @RequestParam(name = "page", defaultValue = "1")
        @Min(1)
        int page
    ) {
        return responseService.getSingleResult(
            adminStoreService.getPendingRequests(page)
        );
    }

    @Override
    @PatchMapping("/{requestId}/approve")
    public SingleResult<UpdateStoreNameApprove> approveStoreName(
        @PathVariable Long requestId
    ) {
        return responseService.getSingleResult(
            adminStoreService.approveStoreName(requestId)
        );
    }

    @Override
    @PatchMapping("/{requestId}/reject")
    public SingleResult<UpdateStoreNameReject> rejectStoreName(
        @PathVariable Long requestId,
        @RequestBody @Valid UpdateStoreNameRejectRequest request
    ) {
        return responseService.getSingleResult(
            adminStoreService.rejectStoreName(requestId, request)
        );
    }
}