package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.config.ranking.BoardLikeInfo;
import com.bbangle.bbangle.config.ranking.ScoreType;
import com.bbangle.bbangle.board.dto.BoardDetailResponseDto;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.service.BoardServiceImpl;
import com.bbangle.bbangle.page.CustomPage;
import com.bbangle.bbangle.util.RedisKeyUtil;
import com.bbangle.bbangle.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
@Tag(name = "Boards", description = "게시판 API")
public class BoardController {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH");

    private final BoardServiceImpl boardService;
    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    @Autowired
    @Qualifier("boardLikeInfoRedisTemplate")
    private final RedisTemplate<String, Object> boardLikeInfoRedisTemplate;

    @Operation(summary = "게시글 전체 조회")
    @ApiResponse(
        responseCode = "200",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CustomPage.class)
        )
    )
    @GetMapping
    public ResponseEntity<CustomPage<List<BoardResponseDto>>> getList(
        @ParameterObject
        FilterRequest filterRequest,
        @RequestParam(required = false, name = "정렬 기준")
        SortType sort,
        @RequestParam(required = false, name = "페이지네이션 cursorId")
        Long cursorId
    ) {
        return ResponseEntity.ok(boardService.getBoardList(
            filterRequest,
            sort,
            cursorId));
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

        redisTemplate.opsForZSet()
            .incrementScore(RedisKeyUtil.POPULAR_KEY, String.valueOf(boardId), 0.1);
        boardLikeInfoRedisTemplate.opsForList()
            .rightPush(LocalDateTime.now()
                    .format(formatter),
                new BoardLikeInfo(boardId, 0.1, LocalDateTime.now(), ScoreType.VIEW));
        redisTemplate.opsForValue()
            .set(viewCountKey, "true", Duration.ofMinutes(3));

        return ResponseEntity.status(HttpStatus.OK)
            .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "상품 상세보기 조회 (1/3)")
    public ResponseEntity<BoardDetailResponseDto> getBoardDetailResponse(
            @PathVariable("id")
            Long boardId
    ) {
        Long memberId = SecurityUtils.getMemberIdWithAnonymous();

        return ResponseEntity.ok().body(
                boardService.getBoardDetailResponse(memberId, boardId)
        );
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

        redisTemplate.opsForZSet()
            .incrementScore(RedisKeyUtil.POPULAR_KEY, String.valueOf(boardId), 1);
        boardLikeInfoRedisTemplate.opsForList()
            .rightPush(LocalDateTime.now()
                    .format(formatter),
                new BoardLikeInfo(boardId, 1, LocalDateTime.now(), ScoreType.PURCHASE));
        redisTemplate.opsForValue()
            .set(purchaseCountKey, "true", Duration.ofMinutes(3));

        return ResponseEntity.status(HttpStatus.OK)
            .build();
    }
}

