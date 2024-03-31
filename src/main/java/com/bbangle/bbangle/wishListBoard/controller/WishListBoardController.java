package com.bbangle.bbangle.wishListBoard.controller;

import com.bbangle.bbangle.wishListBoard.dto.WishProductRequestDto;
import com.bbangle.bbangle.wishListBoard.service.WishListBoardService;
import com.bbangle.bbangle.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boards/{boardId}/wish")
@RequiredArgsConstructor
public class WishListBoardController {

    private final WishListBoardService wishListBoardService;

    @PostMapping
    public ResponseEntity<Void> wish(
        @PathVariable
        Long boardId,
        @RequestBody @Valid
        WishProductRequestDto wishRequest
    ) {
        Long memberId = SecurityUtils.getMemberId();
        wishListBoardService.wish(memberId, boardId, wishRequest);

        return ResponseEntity.status(HttpStatus.OK)
            .build();
    }

    @PutMapping
    public ResponseEntity<Void> cancel(
        @PathVariable
        Long boardId
    ) {
        Long memberId = SecurityUtils.getMemberId();
        wishListBoardService.cancel(memberId, boardId);

        return ResponseEntity.status(HttpStatus.OK)
            .build();
    }

}
