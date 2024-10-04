package com.bbangle.bbangle;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@ActiveProfiles("test")
public class DatabaseCleaner implements InitializingBean {

    @PersistenceContext
    private EntityManager em;
    private List<String> tableNames;

    @Override
    public void afterPropertiesSet() {
        tableNames = em.getMetamodel()
                .getEntities().stream()
                .filter(entity -> entity.getJavaType().getAnnotation(Entity.class) != null)
                .map(entity -> {
                    Table tableAnnotation = entity.getJavaType().getAnnotation(Table.class);
                    if (tableAnnotation != null) {
                        return tableAnnotation.name();
                    } else {
                        return entity.getJavaType().getSimpleName().toLowerCase(); // 기본 테이블 이름을 사용
                    }})
                .toList();
    }

    @Transactional
    public void clear() {
        em.clear();
        em.flush();
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

        tableNames.forEach(
                tableName -> {
                    em.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
                    em.createNativeQuery("ALTER TABLE " + tableName + " ALTER COLUMN id RESTART WITH 1").executeUpdate();
                }
        );

        em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

    @Transactional
    public void clearMember() {
        em.clear();
        em.flush();
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
        em.createNativeQuery("TRUNCATE TABLE member").executeUpdate();
        em.createNativeQuery("ALTER TABLE member ALTER COLUMN id RESTART WITH 2").executeUpdate();
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
    }

}
