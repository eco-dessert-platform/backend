package com.bbangle.bbangle.auth.oauth.client;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.member.domain.Member;

public interface OAuthMemberClient {

    Member fetch(String code);

    OauthServerType supportServer();


}
