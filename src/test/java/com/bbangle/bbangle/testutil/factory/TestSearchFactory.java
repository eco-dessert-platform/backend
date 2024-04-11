package com.bbangle.bbangle.testutil.factory;

import com.bbangle.bbangle.search.domain.Search;
import com.bbangle.bbangle.search.repository.SearchRepository;
import jakarta.persistence.EntityManager;

public class TestSearchFactory extends TestModelFactory<Search, SearchRepository> {
    public TestSearchFactory(EntityManager entityManager, SearchRepository repository) {
        super(entityManager, repository);
    }

    @Override
    protected Search saveEntity(Search entity) {
        return repository.save(entity);
    }
}
