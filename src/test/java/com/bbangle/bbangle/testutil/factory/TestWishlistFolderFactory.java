package com.bbangle.bbangle.testutil.factory;

import com.bbangle.bbangle.wishListFolder.domain.WishlistFolder;
import com.bbangle.bbangle.wishListFolder.repository.WishListFolderRepository;
import jakarta.persistence.EntityManager;

public class TestWishlistFolderFactory extends TestModelFactory<WishlistFolder, WishListFolderRepository> {
    public TestWishlistFolderFactory(EntityManager entityManager, WishListFolderRepository repository) {
        super(entityManager, repository);
    }

    @Override
    protected WishlistFolder saveEntity(WishlistFolder entity) {
        return repository.save(entity);
    }
}
