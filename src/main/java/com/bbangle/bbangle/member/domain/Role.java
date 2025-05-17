package com.bbangle.bbangle.member.domain;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum Role {

    ROLE_USER("ROLE_USER", "사용자"),
    ROLE_SELLER("ROLE_SELLER", "판매자"),
    ROLE_ADMIN("ROLE_ADMIN", "빵그리오븐관리자 ");

    private final String role;
    private final String description;


    Role(String role, String description) {
        this.role = role;
        this.description = description;
    }

    public static Role from(String role) {
        return Arrays.stream(Role.values())
                .filter(r -> r.getRole().equals(role))
                .findFirst()
                .orElse(ROLE_USER);
    }
    
}
