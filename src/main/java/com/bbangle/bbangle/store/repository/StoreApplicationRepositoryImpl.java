package com.bbangle.bbangle.store.repository;

import static com.bbangle.bbangle.store.domain.QStoreApplication.storeApplication;

import com.bbangle.bbangle.store.domain.StoreApplication;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreApplicationRepositoryImpl implements StoreApplicationQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    // TODO : 테스트
    @Override
    public Optional<StoreApplication> findLatestBySellerId(Long sellerId) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(storeApplication)
                .join(storeApplication.seller).fetchJoin()
                .leftJoin(storeApplication.store).fetchJoin()
                .where(storeApplication.seller.id.eq(sellerId))
                .orderBy(storeApplication.createdAt.desc())
                .limit(1)
                .fetchOne()
        );
    }
}
