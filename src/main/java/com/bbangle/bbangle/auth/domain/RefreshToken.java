package com.bbangle.bbangle.auth.domain;

import com.bbangle.bbangle.common.role.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(
        name = "refresh_token",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_id_user_role",
                        columnNames = {"user_id", "user_role"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private Role userRole;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    private RefreshToken(Long userId, Role userRole, String refreshToken) {
        this.userId = userId;
        this.userRole = userRole;
        this.refreshToken = refreshToken;
    }

    public static RefreshToken create(Long userId, Role role, String refreshToken) {
        return new RefreshToken(userId, role, refreshToken);
    }

    public void update(String newRefreshToken) {
        this.refreshToken = newRefreshToken;
    }

    public boolean isAdmin() {
        return this.userRole == Role.ROLE_ADMIN;
    }

    public boolean isCustomer() {
        return this.userRole == Role.ROLE_CUSTOMER;
    }

    public boolean isSeller() {
        return this.userRole == Role.ROLE_SELLER;
    }

}
