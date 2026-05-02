package com.bbangle.bbangle.claim.repository;

import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.repository.custom.ReturnRequestCustomRepository;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long>, ReturnRequestCustomRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ReturnRequest> findWithLockById(Long id);

}
