package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication;
import com.bbangle.bbangle.store.domain.StoreApplication;
import java.util.List;
import java.util.Optional;

public interface StoreApplicationQueryDSLRepository {

    Optional<StoreApplication> findLatestBySellerId(Long sellerId);

    List<AdminSellerApplication> findSellerApplications(int offset, int limit);
}
