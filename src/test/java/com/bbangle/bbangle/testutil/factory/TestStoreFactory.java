package com.bbangle.bbangle.testutil.factory;

import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;

public class TestStoreFactory extends TestModelFactory<Store, StoreRepository> {
    public TestStoreFactory(EntityManager entityManager, StoreRepository repository) {
        super(entityManager, repository);
    }

    @Override
    protected Store saveEntity(Store store) {
        return repository.save(store);
    }
}
