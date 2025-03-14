package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.dto.BoardResponse;
import com.bbangle.bbangle.board.service.RandomBoardService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.page.CursorPageResponse;
import com.bbangle.bbangle.redis.RedisService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Boards-Random", description = "실험에서 사용하기 위한 random data set을 내려주는 Controller")
@RequiredArgsConstructor
public class RandomBoardController {

    private final RandomBoardService randomBoardService;
    private final ResponseService responseService;
    private final RedisService redisService;

    @GetMapping("/boards-random")
    public CommonResult getList(
        @RequestParam(required = false, value = "cursorId")
        Long cursorId,
        @AuthenticationPrincipal
        Long memberId
    ) {
        Integer setNumber = redisService.getSetNumber(memberId);
        CursorPageResponse<BoardResponse> boardResponseList = randomBoardService.getRandomBoardList(
            cursorId,
            memberId,
            setNumber);
        return responseService.getSingleResult(boardResponseList);
    }
}
