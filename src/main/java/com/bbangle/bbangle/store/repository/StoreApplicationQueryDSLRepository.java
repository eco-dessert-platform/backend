package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.store.domain.StoreApplication;
import java.util.Optional;

public interface StoreApplicationQueryDSLRepository {

    Optional<StoreApplication> findLatestBySellerId(Long sellerId);
}
