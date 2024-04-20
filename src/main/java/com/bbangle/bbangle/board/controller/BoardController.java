package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.dto.BoardDetailResponseDto;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.CursorInfo;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.service.BoardService;
import com.bbangle.bbangle.common.message.MessageResDto;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.page.CustomPage;
import com.bbangle.bbangle.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
@Tag(name = "Boards", description = "게시판 API")
public class BoardController {

    private final BoardService boardService;
    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    @Operation(summary = "게시글 전체 조회")
    @ApiResponse(
        responseCode = "200",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CustomPage.class)
        )
    )
    @GetMapping
    public ResponseEntity<BoardCustomPage<List<BoardResponseDto>>> getList(
        @ParameterObject
        FilterRequest filterRequest,
        @RequestParam(required = false)
        SortType sort,
        @ParameterObject
        CursorInfo cursorInfo,
        @AuthenticationPrincipal
        Long memberId
    ) {
        return ResponseEntity.ok(boardService.getBoardList(
            filterRequest,
            sort,
            cursorInfo,
            memberId));
    }

    @GetMapping("/folders/{folderId}")
    public ResponseEntity<Slice<BoardResponseDto>> getPostInFolder(
        @RequestParam(required = false)
        String sort,
        @PathVariable
        Long folderId,
        @PageableDefault
        Pageable pageable
    ) {
        Long memberId = SecurityUtils.getMemberId();
        return ResponseEntity.ok(boardService.getPostInFolder(memberId, sort, folderId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardDetailResponseDto> getBoardDetailResponse(
        @PathVariable("id")
        Long boardId
    ) {
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();

        return ResponseEntity.ok().body(
            boardService.getBoardDetailResponse(memberId, boardId)
        );
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<Void> countView(
        @PathVariable
        Long boardId, HttpServletRequest request
    ) {
        String ipAddress = request.getRemoteAddr();
        String viewCountKey = "VIEW:" + boardId + ":" + ipAddress;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(viewCountKey))) {
            return ResponseEntity.badRequest()
                .build();
        }

        boardService.updateCountView(boardId, viewCountKey);

        return ResponseEntity.status(HttpStatus.OK)
            .build();
    }

    @PatchMapping("/{boardId}/purchase")
    public ResponseEntity<Void> movePurchasePage(
        @PathVariable
        Long boardId, HttpServletRequest request
    ) {
        String ipAddress = request.getRemoteAddr();
        String purchaseCountKey = "PURCHASE:" + boardId + ":" + ipAddress;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(purchaseCountKey))) {
            return ResponseEntity.badRequest()
                .build();
        }

        boardService.adaptPurchaseReaction(boardId, purchaseCountKey);

        return ResponseEntity.status(HttpStatus.OK)
            .build();
    }

    @PatchMapping(value = "/{boardId}/detail", consumes = {"multipart/form-data"})
    public ResponseEntity<Object> putBoardDetailUrl(
        @PathVariable("boardId")
        Long boardId,
        @RequestParam("htmlFile")
        MultipartFile htmlFile
    ) {
        String successMessage = "파일 저장에 성공하셨습니다";
        String failMessage = "파일 저장에 실패하셨습니다";

        if (boardService.saveBoardDetailHtml(boardId, htmlFile)) {
            return ResponseEntity.ok()
                .body(MessageResDto.builder()
                    .message(successMessage)
                    .build()
                );
        }

        // 예상치 못한 에러 발생
        return ResponseEntity.ok()
            .body(MessageResDto.builder()
                .message(failMessage)
                .build());
    }

}

