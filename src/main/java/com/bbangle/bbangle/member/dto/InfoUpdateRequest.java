package com.bbangle.bbangle.member.dto;

import com.bbangle.bbangle.member.domain.Sex;

public record InfoUpdateRequest(
    String nickname,
    Sex sex,
    String birthDate
) {

}
