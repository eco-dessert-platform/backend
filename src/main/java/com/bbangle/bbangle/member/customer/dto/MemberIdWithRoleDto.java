package com.bbangle.bbangle.member.customer.dto;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberIdWithRoleDto {

    private Long memberId;
    private Role role;

    public static MemberIdWithRoleDto from(Member entity) {
        return new MemberIdWithRoleDto(entity.getId(), entity.getRole());
    }
}
