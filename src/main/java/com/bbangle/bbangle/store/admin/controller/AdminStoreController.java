package com.bbangle.bbangle.store.admin.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.AdminApiPath;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest.UpdateStoreNameRejectRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.StoreDetailResponse;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.StoreSearchResult;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameApprove;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameReject;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.admin.controller.swagger.AdminStoreApi;
import com.bbangle.bbangle.store.admin.facade.AdminStoreFacade;
import com.bbangle.bbangle.store.admin.service.AdminStoreService;
import com.bbangle.bbangle.store.admin.service.model.RegisteredStoreInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(AdminApiPath.PREFIX + "/stores")
@Validated
public class AdminStoreController implements AdminStoreApi {

    private final ResponseService responseService;
    private final AdminStoreService adminStoreService;
    private final AdminStoreFacade adminStoreFacade;

    @Override
    @PostMapping(
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public SingleResult<StoreDetailResponse> createStoreForAdmin(
        @Valid @RequestPart("request") AdminStoreRequest.StoreDetailRequest request,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return responseService.getSingleResult(
            adminStoreFacade.createStoreForAdmin(request, profileImage)
        );
    }

    @Override
    @PatchMapping("/{storeId}")
    public SingleResult<StoreDetailResponse> updateStoreForAdmin(
        @PathVariable Long storeId,
        @Valid @RequestBody AdminStoreRequest.StoreDetailRequest request
    ) {
        return responseService.getSingleResult(
            adminStoreService.updateStoreWithName(storeId, request)
        );
    }

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
    @GetMapping("/registered")
    public SingleResult<BbanglePageResponse<RegisteredStoreInfo>> getRegisteredStores(
        @ParameterObject
        @PageableDefault(size = 20, page = 0, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return responseService.getSingleResult(
            BbanglePageResponse.of(adminStoreService.getRegisteredStores(pageable))
        );
    }

    @Override
    @DeleteMapping
    public CommonResult deleteStores(@RequestParam List<Long> storeIds) {
        adminStoreService.deleteStores(storeIds);
        return responseService.getSuccessResult();
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

    @Override
    @GetMapping("/check-name")
    public SingleResult<Boolean> duplicateStoreName(
        @RequestParam String storeName
    ) {
        return responseService.getSingleResult(
            adminStoreService.isDuplicateStoreName(storeName)
        );
    }
}