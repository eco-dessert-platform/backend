package com.bbangle.bbangle.claim.repository;

import com.bbangle.bbangle.claim.domain.Claim;
import com.bbangle.bbangle.claim.repository.custom.ClaimCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long>, ClaimCustomRepository {
}
