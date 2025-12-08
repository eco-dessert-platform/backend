package com.bbangle.bbangle.config.security;

import com.bbangle.bbangle.common.role.Role;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BbangleUserPrincipal {

    private Long id;
    private Role role;

    public static BbangleUserPrincipal of(Long id, Role role) {
        return new BbangleUserPrincipal(id, role);
    }

}
