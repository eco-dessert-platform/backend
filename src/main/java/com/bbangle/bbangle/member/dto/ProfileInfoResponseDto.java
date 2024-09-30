package com.bbangle.bbangle.member.dto;

import com.bbangle.bbangle.member.domain.Sex;
import lombok.Builder;

@Builder
public record ProfileInfoResponseDto(
    String profileImg,
    String nickname,
    String birthDate,
    Sex sex
) {

}
