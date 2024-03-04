package com.bbangle.bbangle.controller;

import com.bbangle.bbangle.dto.MessageResDto;
import com.bbangle.bbangle.dto.ProfileInfoResponseDto;
import com.bbangle.bbangle.exception.ExceedNicknameLengthException;
import com.bbangle.bbangle.service.impl.ProfileServiceImpl;
import com.bbangle.bbangle.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/profile")
public class ProfileController {

    private final ProfileServiceImpl profileService;

    /**
     * 프로필 조회
     *
     * @return 프로필 정보
     */
    @GetMapping
    public ResponseEntity<ProfileInfoResponseDto> getProfile(){
        Long memberId = SecurityUtils.getMemberId();
        return ResponseEntity.ok().body(profileService.getProfileInfo(memberId));
    }


    /**
     * 닉네임 중복 확인
     *
     * @param nickname 닉네임
     * @return 메세지
     */
    @PostMapping("/doublecheck")
    public ResponseEntity<MessageResDto> doubleCheckNickname(@RequestParam String nickname){
        Long memberId = SecurityUtils.getMemberId();
        Assert.notNull(memberId, "권한이 없습니다");
        Assert.notNull(nickname, "닉네임을 입력해주세요");
        if(nickname.length() > 20){
            throw new ExceedNicknameLengthException();
        }
        profileService.doubleCheckNickname(nickname);
        return ResponseEntity.ok().body(new MessageResDto("사용가능한 닉네임이에요!"));
    }
}