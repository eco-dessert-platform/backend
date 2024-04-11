package com.bbangle.bbangle.testutil.factory;

import com.bbangle.bbangle.wishListStore.domain.WishlistStore;
import com.bbangle.bbangle.wishListStore.repository.WishListStoreRepository;
import jakarta.persistence.EntityManager;

public class TestWishlistStoreFactory extends TestModelFactory<WishlistStore, WishListStoreRepository> {
    public TestWishlistStoreFactory(EntityManager entityManager, WishListStoreRepository repository) {
        super(entityManager, repository);
    }

    @Override
    protected WishlistStore saveEntity(WishlistStore entity) {
        return repository.save(entity);
    }
}
