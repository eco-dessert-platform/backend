package com.bbangle.bbangle.auth.oauth.client.dto;

import com.bbangle.bbangle.auth.oauth.OauthServerType;

public interface OAuth2Response {

    /**
     * 소셜 로그인 서버 종류 반환
     * @return OauthServerType
     */
    OauthServerType getProvider();

    String getProviderId();

    /**
     * 소셜 로그인 계정의 Email
     * @return String
     */
    String getEmail();

    /**
     * 소셜 로그인 게정의 이름
     * @return String
     */
    String getName();

    /**
     * 소셜 로그인 게정의 닉네임
     * @return String
     */
    String getNickname();

    /**
     * 소셜 로그인 계정의 프로필 사진
     * @return String
     */
    String getProfile();
}
