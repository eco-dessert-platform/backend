package com.bbangle.bbangle.token.oauth.domain.client;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;

public interface OAuthMemberClient {

    Member fetch(String code);

    OauthServerType supportServer();


}
