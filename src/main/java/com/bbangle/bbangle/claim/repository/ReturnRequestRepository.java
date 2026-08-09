package com.bbangle.bbangle.claim.repository;

import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.repository.custom.ReturnRequestCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long>, ReturnRequestCustomRepository {

}
