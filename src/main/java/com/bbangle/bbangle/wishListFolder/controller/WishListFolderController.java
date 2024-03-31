package com.bbangle.bbangle.wishListFolder.controller;

import com.bbangle.bbangle.wishListFolder.dto.FolderRequestDto;
import com.bbangle.bbangle.wishListFolder.dto.FolderResponseDto;
import com.bbangle.bbangle.wishListFolder.dto.FolderUpdateDto;
import com.bbangle.bbangle.util.SecurityUtils;
import com.bbangle.bbangle.wishListFolder.service.WishListFolderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseEntity<Void> make(
        @RequestBody
        @Valid FolderRequestDto requestDto
    ) {
        Long memberId = SecurityUtils.getMemberId();
        folderService.create(memberId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .build();
    }

    @GetMapping
    public ResponseEntity<List<FolderResponseDto>> getList() {
        Long memberId = SecurityUtils.getMemberId();
        return ResponseEntity.ok(folderService.getList(memberId));
    }

    @PatchMapping("/{folderId}")
    public ResponseEntity<FolderResponseDto> update(
        @PathVariable
        Long folderId,
        @RequestBody
        @Valid FolderUpdateDto updateDto
    ) {
        Long memberId = SecurityUtils.getMemberId();
        folderService.update(memberId, folderId, updateDto);
        return ResponseEntity.status(HttpStatus.OK)
            .build();
    }

    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> delete(
        @PathVariable
        Long folderId
    ) {
        Long memberId = SecurityUtils.getMemberId();
        folderService.delete(folderId, memberId);
        return ResponseEntity.status(HttpStatus.OK)
            .build();
    }

}
