package com.bbangle.bbangle.common.redis.repository;


import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.common.role.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserIdAndUserRole(Long adminId, Role role);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    void deleteByUserIdAndUserRole(Long adminId, Role role);

}
