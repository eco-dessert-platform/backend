package com.bbangle.bbangle.board.admin.controller;

import com.bbangle.bbangle.board.admin.controller.dto.AdminProductResponse;
import com.bbangle.bbangle.board.admin.controller.dto.AdminRemoveProductRequest;
import com.bbangle.bbangle.board.admin.controller.dto.UploadApprovalResponse;
import com.bbangle.bbangle.board.admin.controller.swagger.AdminBoardApi;
import com.bbangle.bbangle.board.admin.service.AdminBoardService;
import com.bbangle.bbangle.board.admin.service.dto.RemoveProductsCommand;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.AdminApiPath;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RequestMapping(AdminApiPath.PREFIX + "/products")
@RestController
public class AdminBoardController implements AdminBoardApi {

    private final AdminBoardService adminBoardService;
    private final ResponseService responseService;

    @Override
    @GetMapping
    public SingleResult<BbanglePageResponse<AdminProductResponse>> getAdminBoards(
        @PageableDefault(size = 20, page = 1, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        Page<AdminProductResponse> result = adminBoardService.getAdminBoards(pageable);
        return responseService.getSingleResult(BbanglePageResponse.of(result));
    }

    @Override
    @DeleteMapping
    public CommonResult deleteBoards(@RequestParam List<Long> productIds) {
        adminBoardService.deleteBoards(productIds);
        return responseService.getSuccessResult();
    }

    @Override
    @DeleteMapping("{productId}/options")
    public CommonResult deleteProducts(
        @PathVariable Long productId,
        @RequestBody @Valid AdminRemoveProductRequest request
    ) {
        RemoveProductsCommand command = new RemoveProductsCommand(productId, request.removeAll(), request.optionIds());
        adminBoardService.deleteProducts(command);
        return responseService.getSuccessResult();
    }

    @Override
    @GetMapping("/upload-approvals")
    public SingleResult<BbanglePageResponse<UploadApprovalResponse>> getUploadApprovals(
        @ParameterObject
        @PageableDefault(size = 20, page = 0, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        Page<UploadApprovalResponse> result = adminBoardService.getUploadApprovals(pageable);
        return responseService.getSingleResult(BbanglePageResponse.of(result));
    }

}
