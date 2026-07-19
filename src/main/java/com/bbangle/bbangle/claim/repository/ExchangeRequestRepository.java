package com.bbangle.bbangle.claim.repository;

import com.bbangle.bbangle.claim.domain.ExchangeRequest;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ExchangeRequest> findWithLockById(Long id);

}
