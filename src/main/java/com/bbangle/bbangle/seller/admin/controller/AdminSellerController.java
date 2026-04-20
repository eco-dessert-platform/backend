package com.bbangle.bbangle.seller.admin.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.AdminApiPath;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerDocumentDownloadRequest;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationList;
import com.bbangle.bbangle.seller.admin.controller.swagger.AdminSellerApi;
import com.bbangle.bbangle.seller.admin.facade.AdminSellerFacade;
import com.bbangle.bbangle.seller.admin.service.AdminSellerDocumentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
}
