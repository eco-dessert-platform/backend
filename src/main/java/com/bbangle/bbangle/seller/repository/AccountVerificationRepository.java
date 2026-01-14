package com.bbangle.bbangle.seller.repository;


import com.bbangle.bbangle.seller.domain.AccountVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountVerificationRepository extends JpaRepository<AccountVerification, Long> {
}
