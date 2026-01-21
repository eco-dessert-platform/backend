package com.bbangle.bbangle.common.redis.repository;


import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.common.role.Role;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserIdAndUserRole(Long adminId, Role role);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    void deleteByUserIdAndUserRole(Long adminId, Role role);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t from RefreshToken t WHERE t.userId = :userId AND t.userRole = :role")
    Optional<RefreshToken> findByUserIdAndUserRoleForUpdate(Long userId, Role role);
}
