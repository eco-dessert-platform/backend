package com.bbangle.bbangle.board.seller.controller;

import com.bbangle.bbangle.board.seller.controller.dto.request.CreateBoardRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.ProductBoardRequest.ProductBoardSearchRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.UpdateBoardRequest;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardSearchResponse;
import com.bbangle.bbangle.board.seller.controller.swagger.SellerBoardApi;
import com.bbangle.bbangle.board.seller.facade.SellerBoardFacade;
import com.bbangle.bbangle.board.seller.service.info.BoardInfo;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(SellerApiPath.PREFIX + "/boards")
public class SellerBoardController implements SellerBoardApi {

    private final ResponseService responseService;
    private final SellerBoardFacade sellerBoardFacade;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public SingleResult<BoardInfo> createBoard(
        @AuthenticationPrincipal Long sellerId,
        @Valid @ModelAttribute CreateBoardRequest request
    ) {
        return responseService.getSingleResult(
            sellerBoardFacade.createBoard(request.toCommand(sellerId))
        );
    }

    @GetMapping("/{storeId}/boards")
    public SingleResult<BbanglePageResponse<SellerBoardSearchResponse>> searchProductBoard(
        @Valid
        @PathVariable(name = "storeId")
        Long storeId,
        ProductBoardSearchRequest request) {

        // TODO : 비즈니스 로직 차후 구현 예정

        PageRequest pageable = PageRequest.of(request.page(), request.size());
        Page<SellerBoardSearchResponse> resultPage = new PageImpl<>(new ArrayList<>(), pageable,
            0);

        BbanglePageResponse<SellerBoardSearchResponse> res = BbanglePageResponse.of(resultPage);
        return responseService.getSingleResult(res);
    }

    @PutMapping(value = "/{boardId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public SingleResult<BoardInfo> changeProductBoard(
        @AuthenticationPrincipal Long sellerId,
        @PathVariable(name = "boardId") Long boardId,
        @Valid @ModelAttribute UpdateBoardRequest request
    ) {
        return responseService.getSingleResult(
            sellerBoardFacade.updateBoard(request.toCommand(sellerId, boardId))
        );
    }

    @PostMapping("/{boardId}/copy")
    public CommonResult copyProductBoard(
        @PathVariable(name = "boardId") Long boardId,
        @RequestParam(name = "storeId") Long storeId) {
        return responseService.getSuccessResult();
    }

    @PostMapping("/delete-boards")
    public CommonResult removeProductBoards(
        @RequestParam(name = "storeId") Long storeId,
        @RequestBody List<Long> boardIds) {
        // TODO: 비즈니스 로직 구현 예정
        return responseService.getSuccessResult();
    }
}
