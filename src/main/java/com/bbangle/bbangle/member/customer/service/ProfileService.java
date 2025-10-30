package com.bbangle.bbangle.member.customer.service;

import com.bbangle.bbangle.member.customer.dto.InfoUpdateRequest;
import com.bbangle.bbangle.member.customer.service.dto.ProfileInfo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileInfo.DefaultProfile getProfileInfo(Long memberId);

    @Transactional
    void updateProfileInfo(
        InfoUpdateRequest request, Long memberId,
        MultipartFile profileImg
    );

    boolean doubleCheckNickname(String nickname);

}
