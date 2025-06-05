package com.bbangle.bbangle.member.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.MessageDto;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.member.dto.MemberInfoRequest;
import com.bbangle.bbangle.member.dto.WithdrawalRequestDto;
import com.bbangle.bbangle.member.service.MemberService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final ResponseService responseService;
    private static final String DELETE_SUCCESS_MSG = "회원 탈퇴에 성공했습니다";

    @PutMapping(value = "additional-information", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResult updateInfo(
            @RequestPart
            MemberInfoRequest additionalInfo,
            @Parameter(
                    description = "프로필 이미지 파일, parameter 명은 profileImg"
            )
            @RequestPart(required = false)
            MultipartFile profileImg,
            @AuthenticationPrincipal
            Long memberId
    ) {
        memberService.updateMemberInfo(additionalInfo, memberId, profileImg);
        return responseService.getSuccessResult();
    }

    @PatchMapping
    public SingleResult<MessageDto> deleteMember(
            @RequestBody WithdrawalRequestDto withdrawalRequestDto,
            @AuthenticationPrincipal
            Long memberId
    ) {
        memberService.saveDeleteReason(withdrawalRequestDto, memberId);
        memberService.deleteMember(memberId);
        return responseService.getSingleResult(new MessageDto(DELETE_SUCCESS_MSG, true));
    }

    @GetMapping("/status")
    public CommonResult getIsAssigned(
            @AuthenticationPrincipal
            Long memberId
    ) {
        return responseService.getSingleResult(memberService.getIsAssigned(memberId));
    }
}
