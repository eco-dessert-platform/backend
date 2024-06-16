package com.bbangle.bbangle.common.controller;

import static com.bbangle.bbangle.exception.BbangleErrorCode.INTERNAL_SERVER_ERROR;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 스웨거 & responseService 사용 예시로 일단 만들어봤습니다
// 1.1 릴리즈전에 삭제예정(지울때 WebOAuthSecurityConfig 여기도 확인)
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/health")
public class HealthCheckController {

  private final ResponseService responseService;

  @GetMapping
  public CommonResult ping() {
    return responseService.getSingleResult("pong");
  }

  @GetMapping("/list")
  public CommonResult pingList() {
    return responseService.getListResult(List.of("pong1", "pong2"));
  }

  @GetMapping("/fail")
  public CommonResult pingFail() {
    return responseService.getFailResult("테스트 실패", -1);
  }

  @GetMapping("/exception/bbangle")
  public CommonResult pingException() {
    throw new BbangleException(INTERNAL_SERVER_ERROR);
  }

  @GetMapping("/exception/normal")
  public CommonResult pingException2() {
    throw new RuntimeException("예상치못한 예외발생 테스트");
  }
}
