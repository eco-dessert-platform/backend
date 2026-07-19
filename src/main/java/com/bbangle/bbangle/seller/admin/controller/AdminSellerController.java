package com.bbangle.bbangle.seller.admin.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.AdminApiPath;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerDocumentDownloadRequest;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerRequest;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerRequest.StoreApplicationApprove;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationApproveList;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationList;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationRejectList;
import com.bbangle.bbangle.seller.admin.controller.swagger.AdminSellerApi;
import com.bbangle.bbangle.seller.admin.facade.AdminSellerFacade;
import com.bbangle.bbangle.seller.admin.service.AdminSellerDocumentService;
import com.bbangle.bbangle.seller.admin.service.AdminSellerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(AdminApiPath.PREFIX + "/sellers")
public class AdminSellerController implements AdminSellerApi {

    private final ResponseService responseService;
    private final AdminSellerFacade adminSellerFacade;
    private final AdminSellerDocumentService adminSellerDocumentService;
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
    @PostMapping("/documents/download")
    public ResponseEntity<StreamingResponseBody> downloadSellerDocuments(
        @RequestBody @Valid AdminSellerDocumentDownloadRequest request
    ) {
        StreamingResponseBody body = out ->
            adminSellerDocumentService.downloadSellerDocuments(request.sellerIds(), out);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documents.zip\"")
            .contentType(MediaType.parseMediaType("application/zip"))
            .body(body);
    }

    @Override
    @PutMapping("/approve")
    public SingleResult<AdminSellerApplicationApproveList> approveSellerApplications(
        @RequestBody @NotEmpty List<@Valid StoreApplicationApprove> requests
    ) {
        return responseService.getSingleResult(
            adminSellerFacade.approveStoreApplications(requests)
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
