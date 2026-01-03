package com.bbangle.bbangle.board.admin.controller;

import com.bbangle.bbangle.board.admin.controller.dto.AdminProductResponse;
import com.bbangle.bbangle.board.admin.controller.swagger.AdminBoardApi;
import com.bbangle.bbangle.board.admin.service.AdminBoardService;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.AdminApiPath;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
