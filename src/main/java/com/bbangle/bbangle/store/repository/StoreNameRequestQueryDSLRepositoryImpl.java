package com.bbangle.bbangle.store.repository;

import static com.bbangle.bbangle.store.domain.QStoreNameRequest.storeNameRequest;

import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreNameRequestQueryDSLRepositoryImpl implements StoreNameRequestQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<StoreApprovalStatus> findActiveRequestsBySellerId(Long sellerId) {

        StoreNameRequest result = queryFactory.selectFrom(storeNameRequest)
            .where(
                storeNameRequest.seller.id.eq(sellerId),
                storeNameRequest.status.in(StoreApprovalStatus.APPROVE, StoreApprovalStatus.PENDING)
            )
            .orderBy(
                new CaseBuilder()
                    .when(storeNameRequest.status.eq(StoreApprovalStatus.APPROVE)).then(1)
                    .when(storeNameRequest.status.eq(StoreApprovalStatus.PENDING)).then(2)
                    .otherwise(3)   // QueryDSL의 CASE 표현식은 otherwise()가 반드시 필요함
                    .asc()
            )
            .fetchFirst();

        return Optional.ofNullable(result)
            .map(StoreNameRequest::getStatus);
    }
}
