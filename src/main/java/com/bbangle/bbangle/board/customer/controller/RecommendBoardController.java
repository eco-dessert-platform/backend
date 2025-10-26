package com.bbangle.bbangle.board.customer.controller;

import com.bbangle.bbangle.board.customer.service.RecommendBoardScheduler;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/boards/recommendation")
@Tag(name = "RecommendBoard", description = "실험에서 사용하기 위한 random data set을 내려주는 Controller")
@RequiredArgsConstructor
public class RecommendBoardController {

    private final ResponseService responseService;
    private final RecommendBoardScheduler recommendBoardScheduler;

    @GetMapping
    public CommonResult startUploadTask() {
        recommendBoardScheduler.startUploadTask();
        return responseService.getSuccessResult();
    }
}
