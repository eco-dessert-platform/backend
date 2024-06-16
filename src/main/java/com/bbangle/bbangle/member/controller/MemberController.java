package com.bbangle.bbangle.member.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.MessageDto;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.member.dto.WithdrawalRequestDto;
import com.bbangle.bbangle.member.dto.MemberInfoRequest;
import com.bbangle.bbangle.member.service.MemberService;
import com.bbangle.bbangle.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final ResponseService responseService;
    private static final String DELETE_SUCCESS_MSG = "회원 탈퇴에 성공했습니다";

    @PutMapping("additional-information")
    public CommonResult updateInfo(
        @RequestPart
        MemberInfoRequest additionalInfo,
        @RequestPart(required = false)
        MultipartFile profileImg,
        @AuthenticationPrincipal
        Long memberId
    ) {
        memberService.updateMemberInfo(additionalInfo, memberId, profileImg);
        return responseService.getSuccessResult();
    }

    @PatchMapping
    public CommonResult deleteMember(
        @RequestBody WithdrawalRequestDto withdrawalRequestDto,
        @AuthenticationPrincipal
        Long memberId
    ){
        memberService.saveDeleteReason(withdrawalRequestDto, memberId);
        memberService.deleteMember(memberId);
        return responseService.getSingleResult(new MessageDto(DELETE_SUCCESS_MSG,true));
    }
}
