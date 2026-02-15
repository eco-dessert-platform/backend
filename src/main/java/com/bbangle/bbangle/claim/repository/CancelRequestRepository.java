package com.bbangle.bbangle.claim.repository;

import com.bbangle.bbangle.claim.domain.CancelRequest;
import com.bbangle.bbangle.claim.repository.custom.CancelRequestCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CancelRequestRepository extends JpaRepository<CancelRequest, Long>, CancelRequestCustomRepository {
}
