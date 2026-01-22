package com.bbangle.bbangle.common.role;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum Role {

    ROLE_CUSTOMER("ROLE_CUSTOMER", "고객"),
    ROLE_SELLER("ROLE_SELLER", "판매자"),
    ROLE_ADMIN("ROLE_ADMIN", "빵그리오븐관리자");

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
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + role));
    }

}
