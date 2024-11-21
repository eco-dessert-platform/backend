package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.dto.SimilarityBoardResponse;
import com.bbangle.bbangle.board.service.BoardDetailService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boards")
@Tag(name = "BoardDetail", description = "게시판 상세정보 API")
@RequiredArgsConstructor
public class BoardDetailController {

    private final BoardDetailService boardDetailService;
    private final ResponseService responseService;

    @GetMapping("/{boardId}/similar_board")
    public CommonResult getCount(
        @PathVariable(value = "boardId")
        Long boardId,
        @AuthenticationPrincipal
        Long memberId
    ) {
        List<SimilarityBoardResponse> similarityBoardResponses = boardDetailService.getSimilarityBoardResponses(
            memberId, boardId);
        return responseService.getSingleResult(similarityBoardResponses);
    }

}

