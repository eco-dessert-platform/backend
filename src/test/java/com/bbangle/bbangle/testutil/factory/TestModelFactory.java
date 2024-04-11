package com.bbangle.bbangle.testutil.factory;

import jakarta.persistence.EntityManager;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public abstract class TestModelFactory<T, U> {
    @Getter protected U repository;
    protected EntityManager entityManager;
    protected Map<String, T> entityMap;

    public TestModelFactory(EntityManager entityManager, U repository) {
        this.entityManager = entityManager;
        this.repository = repository;
        this.entityMap = new HashMap<>();
    }

    public void resetEntityMap(){
        this.entityMap = new HashMap<>();
    }

    public T getTestEntity(String label) {
        return entityMap.get(label);
    }

    public T pushTestEntity(String label, T entity) {
        T savedEntity = saveEntity(entity);
        entityMap.put(label, savedEntity);
        return savedEntity;
    }

    protected abstract T saveEntity(T entity);
}
