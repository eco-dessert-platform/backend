package com.bbangle.bbangle.testutil.factory;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.ProductRepository;
import jakarta.persistence.EntityManager;

public class TestProductFactory extends TestModelFactory<Product, ProductRepository> {
    public TestProductFactory(EntityManager entityManager, ProductRepository repository) {
        super(entityManager, repository);
    }

    @Override
    protected Product saveEntity(Product entity) {
        return repository.save(entity);
    }
}
