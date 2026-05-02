package com.bbangle.bbangle.seller.repository;


import com.bbangle.bbangle.seller.domain.AccountVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountVerificationRepository extends JpaRepository<AccountVerification, Long> {

    Optional<AccountVerification> findBySellerId(Long sellerId);
}
