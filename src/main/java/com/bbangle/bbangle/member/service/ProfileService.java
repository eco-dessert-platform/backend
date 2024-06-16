package com.bbangle.bbangle.member.service;

import com.bbangle.bbangle.member.dto.InfoUpdateRequest;
import com.bbangle.bbangle.member.dto.ProfileInfoResponseDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileInfoResponseDto getProfileInfo(Long memberId);

    @Transactional
    void updateProfileInfo(
        InfoUpdateRequest request, Long memberId,
        MultipartFile profileImg
    );

    boolean doubleCheckNickname(String nickname);

}
