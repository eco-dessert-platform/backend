package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList.SellerApplicationInfo;
import com.bbangle.bbangle.store.domain.StoreApplication;
import java.util.List;
import java.util.Optional;

public interface StoreApplicationQueryDSLRepository {

    Optional<StoreApplication> findLatestBySellerId(Long sellerId);

    List<SellerApplicationInfo> findSellerApplications(int offset, int limit);

    long countSellerApplications();
}
