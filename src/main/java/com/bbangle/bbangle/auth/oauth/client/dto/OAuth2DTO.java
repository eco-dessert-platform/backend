package com.bbangle.bbangle.auth.oauth.client.dto;

import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import java.util.List;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OAuth2DTO {

    // profile 파라미터 값이 부적절한 경우 prod로 고정
    public static String defaultProfile(String profile) {
        if (profile == null || profile.isBlank()) return ProfileType.PROD.name().toLowerCase();

        try {
            return ProfileType.valueOf(profile.toUpperCase())
                .name()
                .toLowerCase();
        } catch (IllegalArgumentException e) {
            return ProfileType.PROD.name().toLowerCase();
        }
    }

    // 실행 중인 서버 프로파일이 prod일 경우 profile 파라미터 값을 prod로 고정
    public static String defaultProfile(List<String> serverProfiles, String profile) {
        boolean isProdServer = serverProfiles.stream()
            .anyMatch(p -> p.equalsIgnoreCase(ProfileType.PROD.name()));

        if (isProdServer) {
            return ProfileType.PROD.name().toLowerCase();
        }

        return profile;
    }

    public enum UserType {CUSTOMER, SELLER} // Customer : Customer 로그인 요청 / Seller : Seller 로그인 요청
    public enum ProfileType {LOCAL, PROD}   // Local : localhost에서 요청 / Prod : 배포 사이트에서 요청

    // OAuth2 로그인 요청 시 전달받은 파라미터 값을 담을 DTO
    @Builder
    public record OAuthParams(
        UserType user,    // customer | seller
        ProfileType profile  // local | prod
    ) {}

    // OAuth2 로그인 성공 후 Redis에 저장할 로그인 User 정보 DTO
    @Builder
    public record InfoDTO(
        Long id,
        Role role,
        CertificationStatus status
    ) {}
}