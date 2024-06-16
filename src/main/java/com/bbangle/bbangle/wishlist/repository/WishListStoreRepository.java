package com.bbangle.bbangle.wishlist.repository;

import com.bbangle.bbangle.wishlist.domain.WishListStore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishListStoreRepository extends JpaRepository<WishListStore, Long>, WishListStoreQueryDSLRepository {
}
