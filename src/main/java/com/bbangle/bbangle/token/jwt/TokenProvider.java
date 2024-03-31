package com.bbangle.bbangle.token.jwt;

import com.bbangle.bbangle.member.domain.Member;
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

/**
 * 토큰 제공 클래스
 */
@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * JWT 토큰 생성
     *
     * @param member    유저
     * @param expiredAt 만료기간
     * @return JWT 토큰
     */
    public String generateToken(Member member, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), member);
    }


    /**
     * JWT 토큰 실제 생성
     *
     * @param expiry 만료 기간
     * @param member 유저
     * @return JWT 토큰
     */
    private String makeToken(Date expiry, Member member) {
        Date now = new Date();

        return Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
            .setIssuer(jwtProperties.getIssuer())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .setSubject(member.getEmail())
            .claim("id", member.getId())
            // 서명 : 비밀값과 함께 해시값을 HS256 방식으로 암호화
            .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
            .compact();
    }

    /**
     * 토큰 유효성 검증
     *
     * @param token the token
     * @return the boolean
     */
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

    /**
     * 토큰 기반으로 인증 정보 가져오기
     *
     * @param token the token
     * @return 인증 정보
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Object memberId = claims.get("id");
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
            new SimpleGrantedAuthority("ROLE_USER"));
        return new UsernamePasswordAuthenticationToken(memberId, token, authorities);
    }

    /**
     * 토큰 클레임 얻기
     *
     * @param token the token
     * @return the claims
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
            .setSigningKey(jwtProperties.getSecretKey())
            .parseClaimsJws(token)
            .getBody();
    }

}
