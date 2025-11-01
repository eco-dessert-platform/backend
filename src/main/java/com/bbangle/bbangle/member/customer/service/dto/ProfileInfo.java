package com.bbangle.bbangle.member.customer.service.dto;

import com.bbangle.bbangle.member.domain.Sex;

public class ProfileInfo {

    public record DefaultProfile(
        String profileImg,
        String nickname,
        String birthDate,
        Sex sex
    ) {
    }
}
