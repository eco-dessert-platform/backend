package com.bbangle.bbangle.seller.repository;

import com.bbangle.bbangle.seller.domain.Seller;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByMember_id(Long memberId);
}
