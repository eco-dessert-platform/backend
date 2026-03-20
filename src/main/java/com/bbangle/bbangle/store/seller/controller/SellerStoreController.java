package com.bbangle.bbangle.store.seller.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest.UpdateStoreDetailRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SellerStoreDetail;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.UpdateStoreNameResponse;
import com.bbangle.bbangle.store.seller.controller.swagger.SellerStoreApi;
import com.bbangle.bbangle.store.seller.facade.SellerStoreApplicationFacade;
import com.bbangle.bbangle.store.seller.facade.SellerStoreFacade;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/stores")
@Validated
public class SellerStoreController implements SellerStoreApi {

    private final ResponseService responseService;
    private final SellerStoreService sellerStoreService;
    private final SellerStoreFacade sellerStoreFacade;
    private final SellerStoreApplicationFacade sellerStoreApplicationFacade;

    @Override
    @PostMapping(
        path = "/applications",
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
    )
    public SingleResult<StoreApplicationResponse.StoreApplicationDetail> registerStore(
        @AuthenticationPrincipal Long sellerId,
        @Valid @RequestPart("request") StoreApplicationRequest.StoreApplicationCreateRequest request,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return responseService.getSingleResult(
            sellerStoreApplicationFacade.registerStoreForSeller(sellerId, request, profileImage)
        );
    }

    @Override
    @GetMapping("/applications")
    public SingleResult<StoreApplicationResponse.StoreApplicationDetail> getStoreApplication(
        @AuthenticationPrincipal Long sellerId
    ) {
        return responseService.getSingleResult(
            sellerStoreApplicationFacade.findStoreApplication(sellerId)
        );
    }

    @Override
    @GetMapping("/store-names")
    public SingleResult<CursorPagination<SellerStoreInfo.StoreInfo>> search(
        @RequestParam @NotBlank(message = "스토어 이름은 필수입니다.") String storeName,
        @RequestParam(required = false) Long cursorId
    ) {
        return responseService.getSingleResult(
            sellerStoreService.selectStoreNameForSeller(storeName, cursorId)
        );
    }

    @Override
    @GetMapping("/check-name")
    public SingleResult<StoreResponse.StoreNameCheck> checkStoreNameDuplicate(
        @RequestParam @NotBlank(message = "스토어 이름은 필수입니다.") String storeName
    ) {
        return responseService.getSingleResult(sellerStoreFacade.checkStoreName(storeName));
    }

    @Override
    @GetMapping()
    public SingleResult<StoreResponse.SellerStoreDTO> getRegisteredStoreDetail(
        @AuthenticationPrincipal Long sellerId
    ) {
        return responseService.getSingleResult(
            sellerStoreFacade.getRegisteredStoreDetail(sellerId)
        );
    }

    @Override
    @PostMapping("/store-names")
    public SingleResult<UpdateStoreNameResponse> updateStoreName(
        @AuthenticationPrincipal Long sellerId,
        @Valid @RequestBody UpdateStoreNameRequest request
    ) {
        return responseService.getSingleResult(sellerStoreFacade.updateStoreName(sellerId, request));
    }

    // TODO : Test
    @Override
    @PutMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public SingleResult<SellerStoreDetail> updateStoreDetail(
        @AuthenticationPrincipal Long sellerId,
        @Valid @RequestPart("request") UpdateStoreDetailRequest request,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return responseService.getSingleResult(sellerStoreFacade.updateStoreDetail(sellerId, request, profileImage));
    }
}
