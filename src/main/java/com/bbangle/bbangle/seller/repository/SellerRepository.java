package com.bbangle.bbangle.seller.repository;

import com.bbangle.bbangle.seller.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long> {
}
