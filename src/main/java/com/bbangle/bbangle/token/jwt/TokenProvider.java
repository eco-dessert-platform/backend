package com.bbangle.bbangle.token.jwt;

import com.bbangle.bbangle.member.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TokenProvider {
    private static final String MEMBER_KEY = "id";
    private static final String ROLE_KEY = "role";
    private final JwtProperties jwtProperties;

    public String generateToken(Long memberId, Role role, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), memberId, role);
    }

    private String makeToken(Date expiry, Long memberId, Role role) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim(MEMBER_KEY, memberId)
                .claim(ROLE_KEY, role.getRole())
                // 서명 : 비밀값과 함께 해시값을 HS256 방식으로 암호화
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())// 비밀값으로 복호화
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false; //복호화 과정에서 에러가 나면 유효하지 않은 토큰
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Long memberId = Long.valueOf((Integer) claims.get(MEMBER_KEY));
        Role role = Role.from((String) claims.get(ROLE_KEY));
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority(role.getRole()));
        return new UsernamePasswordAuthenticationToken(memberId, token, authorities);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }

}
