package com.bbangle.bbangle.member.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.NOTFOUND_MEMBER;
import static com.bbangle.bbangle.image.domain.ImageCategory.MEMBER_PROFILE;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.service.ImageService;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.dto.InfoUpdateRequest;
import com.bbangle.bbangle.member.dto.ProfileInfoResponseDto;
import com.bbangle.bbangle.member.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final ImageService imageService;

    @Override
    public ProfileInfoResponseDto getProfileInfo(Long memberId) {
        Member member = profileRepository.findById(memberId)
            .orElseThrow(() -> new BbangleException(NOTFOUND_MEMBER));
        return ProfileInfoResponseDto.builder()
            .profileImg(member.getProfile())
            .nickname(member.getNickname())
            .sex(member.getSex())
            .birthDate(member.getBirth())
            .build();
    }

    @Transactional
    @Override
    public void updateProfileInfo(
        InfoUpdateRequest request,
        Long memberId,
        MultipartFile profileImg
    ) {
        Member member = profileRepository.findById(memberId)
            .orElseThrow(() -> new BbangleException(NOTFOUND_MEMBER));
        if (profileImg != null && !profileImg.isEmpty()) {
            String imgUrl = imageService.save(MEMBER_PROFILE, profileImg, memberId);
            member.updateProfile(imgUrl);
        }
        member.update(request);
    }

    @Override
    public boolean doubleCheckNickname(String nickname) {
        return profileRepository.findByNickname(nickname).isPresent();
    }
}
