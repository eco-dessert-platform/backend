package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/board/recommendation")
@Tag(name = "Boards-Random", description = "실험에서 사용하기 위한 random data set을 내려주는 Controller")
@RequiredArgsConstructor
public class RecommendBoardController {

    private final ResponseService responseService;

    @GetMapping
    public CommonResult get() {


        return responseService.getSuccessResult();
    }
}
