package com.bbangle.bbangle.token.oauth.domain.client;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class OauthMemberClientComposite {

    private final Map<OauthServerType, OAuthMemberClient> mapping;

    public OauthMemberClientComposite(Set<OAuthMemberClient> clients) {
        mapping = clients.stream()
            .collect(toMap(
                OAuthMemberClient::supportServer,
                identity()
            ));
    }

    public Member fetch(OauthServerType oauthServerType, String authCode) {
        return getClient(oauthServerType).fetch(authCode);
    }

    private OAuthMemberClient getClient(OauthServerType oauthServerType) {
        return Optional.ofNullable(mapping.get(oauthServerType))
            .orElseThrow(() -> new RuntimeException("지원하지 않는 소셜 로그인 타입입니다"));
    }

}
