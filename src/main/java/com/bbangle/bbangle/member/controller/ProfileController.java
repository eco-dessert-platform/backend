package com.bbangle.bbangle.member.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.MessageDto;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.member.dto.InfoUpdateRequest;
import com.bbangle.bbangle.member.dto.ProfileInfoResponseDto;
import com.bbangle.bbangle.member.service.ProfileService;
import com.bbangle.bbangle.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final ResponseService responseService;
    private static final String TYPING_NICKNAME = "닉네임을 입력해주세요!";
    private static final String RESTRICT_NICKNAME_20 = "닉네임은 20자 제한이에요!";
    private static final String DUPLICATE_NICKNAME = "중복된 닉네임이에요";
    private static final String AVAILABLE_NICKNAME = "사용가능한 닉네임이에요!";

    /**
     * 프로필 조회
     *
     * @return 프로필 정보
     */
    @GetMapping
    public CommonResult getProfile() {
        Long memberId = SecurityUtils.getMemberId();
        ProfileInfoResponseDto profileInfo = profileService.getProfileInfo(memberId);
        return responseService.getSingleResult(profileInfo);
    }


    /**
     * 닉네임 중복 확인
     *
     * @param nickname 닉네임
     * @return 메세지
     */
    @GetMapping("/doublecheck")
    public CommonResult doubleCheckNickname(
        @RequestParam("nickname") String nickname
    ) {
        Long memberId = SecurityUtils.getMemberId();
        Assert.notNull(memberId, "권한이 없습니다");

        if (nickname == null || StringUtils.isBlank(nickname) || nickname.contains("\t")
            || nickname.contains("\n")) {
            return responseService.getSingleResult(new MessageDto(TYPING_NICKNAME, false));
        }
        if (nickname.length() > 20) {
            return responseService.getSingleResult(new MessageDto(RESTRICT_NICKNAME_20, false));
        }

        if (profileService.doubleCheckNickname(nickname)) {
            return responseService.getSingleResult(new MessageDto(DUPLICATE_NICKNAME, false));
        }
        return responseService.getSingleResult(new MessageDto(AVAILABLE_NICKNAME, true));
    }

    @PutMapping
    public CommonResult update(
        @RequestPart(required = false)
        InfoUpdateRequest infoUpdateRequest,
        @RequestPart(required = false)
        MultipartFile profileImg
    ) {
        Long memberId = SecurityUtils.getMemberId();
        profileService.updateProfileInfo(infoUpdateRequest, memberId, profileImg);
        return responseService.getSuccessResult();
    }
}
