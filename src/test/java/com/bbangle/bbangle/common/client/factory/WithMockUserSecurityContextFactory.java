package com.bbangle.bbangle.common.client.factory;

import com.bbangle.bbangle.common.client.annotation.WithMockAuthenticationPrincipal;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockUserSecurityContextFactory implements WithSecurityContextFactory<WithMockAuthenticationPrincipal> {

    @Override
    public SecurityContext createSecurityContext(WithMockAuthenticationPrincipal annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            annotation.userId(),
            annotation.credentials(),
            List.of(
                new SimpleGrantedAuthority("ROLE_" + annotation.role())
            )
        );

        context.setAuthentication(authentication);
        return context;
    }
}
