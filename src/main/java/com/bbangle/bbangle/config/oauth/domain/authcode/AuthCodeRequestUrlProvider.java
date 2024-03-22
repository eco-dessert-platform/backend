package com.bbangle.bbangle.config.oauth.domain.authcode;

import com.bbangle.bbangle.config.oauth.domain.OauthServerType;

public interface AuthCodeRequestUrlProvider {

    OauthServerType supportServer(); //어떤 OauthServerType을 지원할 수 있는지
    String provide();
}
