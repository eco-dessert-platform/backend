package com.bbangle.bbangle.admin.controller;

import com.bbangle.bbangle.admin.dto.AdminBoardImgResponseDto;
import com.bbangle.bbangle.admin.dto.AdminBoardRequestDto;
import com.bbangle.bbangle.admin.dto.AdminBoardResponseDto;
import com.bbangle.bbangle.admin.dto.AdminProductRequestDto;
import com.bbangle.bbangle.admin.dto.AdminStoreRequestDto;
import com.bbangle.bbangle.admin.dto.AdminStoreResponseDto;
import com.bbangle.bbangle.admin.service.AdminService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ResponseService responseService;

    @PostMapping(value = "/store", consumes = {"multipart/form-data", "application/json"})
    public CommonResult uploadStore(
        @ModelAttribute
        AdminStoreRequestDto adminStoreRequestDto,
        @RequestPart("profile")
        MultipartFile profile
    ) {
        AdminStoreResponseDto adminStore =
                    AdminStoreResponseDto.builder()
                        .storeId(adminService.uploadStore(adminStoreRequestDto, profile))
                        .build();
        return responseService.getSingleResult(adminStore);
    }

    @PostMapping(
        value = "/stores/{storeId}/board", consumes = {"multipart/form-data", "application/json"}
    )
    public CommonResult uploadBoard(
        @PathVariable("storeId")
        Long storeId,
        @ModelAttribute
        AdminBoardRequestDto adminBoardRequestDto,
        @RequestPart("profile")
        MultipartFile profile
    ) {
        AdminBoardResponseDto adminBoard =
                    AdminBoardResponseDto.builder()
                        .boardId(adminService.uploadBoard(profile, storeId, adminBoardRequestDto))
                        .build();
        return responseService.getSingleResult(adminBoard);
    }

    @PostMapping(
        value = "/stores/{storeId}/boards/{boardId}/subimage",
        consumes = {"multipart/form-data", "application/json"}
    )
    public CommonResult uploadBoardImg(
        @PathVariable("storeId")
        Long storeId,
        @PathVariable("boardId")
        Long boardId,
        @RequestPart("subimage")
        MultipartFile subImage
    ) {
        AdminBoardImgResponseDto adminBoardImg =
                    AdminBoardImgResponseDto.builder()
                    .message(adminService.uploadBoardImage(storeId, boardId, subImage) ? "저장 성공"
                        : "저장 실패")
                    .build();
        return responseService.getSingleResult(adminBoardImg);
    }

    @PostMapping(value = "/stores/{storeId}/boards/{boardId}/product")
    public CommonResult uploadProduct(
        @PathVariable("storeId")
        Long storeId,
        @PathVariable("boardId")
        Long boardId,
        @RequestBody
        AdminProductRequestDto adminProductRequestDto
    ) {
        adminService.uploadProduct(storeId, boardId, adminProductRequestDto);
        AdminBoardImgResponseDto adminProduct =
                    AdminBoardImgResponseDto.builder()
                        .message("저장 성공")
                        .build();
        return responseService.getSingleResult(adminProduct);
    }
}
