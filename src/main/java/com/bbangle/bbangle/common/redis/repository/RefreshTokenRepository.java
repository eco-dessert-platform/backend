package com.bbangle.bbangle.common.redis.repository;


import com.bbangle.bbangle.token.domain.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    //클라이언트에 저장된 멤버 id를 가져와 리프레시 토큰을 확인하고 액세스 토큰을 발급한다
    Optional<RefreshToken> findByMemberId(Long memberId);

    Optional<RefreshToken> findByAdminId(Long adminId);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    void deleteByAdminId(Long adminId);

}
