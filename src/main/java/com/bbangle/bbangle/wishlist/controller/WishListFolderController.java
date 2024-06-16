package com.bbangle.bbangle.wishlist.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.wishlist.dto.FolderRequestDto;
import com.bbangle.bbangle.wishlist.dto.FolderUpdateDto;
import com.bbangle.bbangle.util.SecurityUtils;
import com.bbangle.bbangle.wishlist.service.WishListFolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wishLists")
public class WishListFolderController {

    private final WishListFolderService folderService;
    private final ResponseService responseService;

    @PostMapping
    public CommonResult make(
        @RequestBody
        FolderRequestDto requestDto,
        @AuthenticationPrincipal
        Long memberId
    ) {
        folderService.create(memberId, requestDto);
        return responseService.getSuccessResult();
    }

    @GetMapping
    public CommonResult getList(
        @AuthenticationPrincipal
        Long memberId
    ) {
        return responseService.getListResult(folderService.getList(memberId));
    }

    @PatchMapping("/{folderId}")
    public CommonResult update(
        @PathVariable
        Long folderId,
        @RequestBody
        FolderUpdateDto updateDto,
        @AuthenticationPrincipal
        Long memberId
    ) {
        folderService.update(memberId, folderId, updateDto);
        return responseService.getSuccessResult();
    }

    @DeleteMapping("/{folderId}")
    public CommonResult delete(
        @PathVariable
        Long folderId
    ) {
        Long memberId = SecurityUtils.getMemberId();
        folderService.delete(folderId, memberId);
        return responseService.getSuccessResult();
    }

}
