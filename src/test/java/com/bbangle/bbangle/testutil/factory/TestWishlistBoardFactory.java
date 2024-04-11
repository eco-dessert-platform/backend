package com.bbangle.bbangle.testutil.factory;

import com.bbangle.bbangle.wishListBoard.domain.WishlistProduct;
import com.bbangle.bbangle.wishListBoard.repository.WishListProductRepository;
import jakarta.persistence.EntityManager;

public class TestWishlistBoardFactory extends TestModelFactory<WishlistProduct, WishListProductRepository> {
    public TestWishlistBoardFactory(EntityManager entityManager, WishListProductRepository repository) {
        super(entityManager, repository);
    }

    @Override
    protected WishlistProduct saveEntity(WishlistProduct entity) {
        return repository.save(entity);
    }
}
