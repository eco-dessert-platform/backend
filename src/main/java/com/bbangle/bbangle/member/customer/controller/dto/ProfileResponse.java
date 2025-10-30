package com.bbangle.bbangle.member.customer.controller.dto;

import com.bbangle.bbangle.member.domain.Sex;

public class ProfileResponse {

    public record DefaultProfile(
        String profileImg,
        String nickname,
        String birthDate,
        Sex sex
    ) {
    }

}
