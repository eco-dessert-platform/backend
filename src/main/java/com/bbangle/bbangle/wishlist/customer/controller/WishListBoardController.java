package com.bbangle.bbangle.wishlist.customer.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.wishlist.customer.dto.WishListBoardRequest;
import com.bbangle.bbangle.wishlist.customer.service.WishListBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boards/{boardId}")
@RequiredArgsConstructor
public class WishListBoardController {

    private final WishListBoardService wishListBoardService;
    private final ResponseService responseService;

    @PostMapping("/wish")
    public CommonResult wish(
        @AuthenticationPrincipal
        Long memberId,
        @PathVariable("boardId")
        Long boardId,
        @RequestBody
        WishListBoardRequest wishRequest
    ) {
        wishListBoardService.wish(memberId, boardId, wishRequest);
        return responseService.getSuccessResult();
    }

    @DeleteMapping("/cancel")
    public CommonResult cancel(
        @AuthenticationPrincipal
        Long memberId,
        @PathVariable("boardId")
        Long boardId
    ) {
        wishListBoardService.cancel(memberId, boardId);
        return responseService.getSuccessResult();
    }

}
