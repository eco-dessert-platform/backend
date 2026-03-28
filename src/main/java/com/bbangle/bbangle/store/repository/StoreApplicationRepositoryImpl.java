package com.bbangle.bbangle.store.repository;

import static com.bbangle.bbangle.seller.domain.QAccountVerification.accountVerification;
import static com.bbangle.bbangle.seller.domain.QSeller.seller;
import static com.bbangle.bbangle.store.domain.QStoreApplication.storeApplication;

import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerInfo;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerStoreInfo;
import com.bbangle.bbangle.seller.domain.QAccountVerification;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StoreApplicationRepositoryImpl implements StoreApplicationQueryDSLRepository {

    private final JPAQueryFactory queryFactory;

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

    @Override
    public List<AdminSellerApplication> findSellerApplications(int offset, int limit) {

        QAccountVerification avSub = new QAccountVerification("avSub");

        return queryFactory
            .select(Projections.constructor(
                AdminSellerApplication.class,

                // storeApplicationId
                storeApplication.id,

                // SellerStoreInfo
                Projections.constructor(
                    SellerStoreInfo.class,
                    storeApplication.name,
                    storeApplication.phoneNumberVO.phoneNumber,
                    storeApplication.phoneNumberVO.subPhoneNumber,
                    storeApplication.emailVO.email,
                    storeApplication.originAddressLine,
                    storeApplication.originAddressDetail
                ),

                // SellerInfo
                Projections.constructor(
                    SellerInfo.class,
                    seller.id,
                    accountVerification.bankCode,
                    accountVerification.accountHolder,
                    accountVerification.accountNumber,
                    seller.createdAt
                )
            ))
            .from(storeApplication)
            .join(storeApplication.seller, seller)

            // ✅ verified = true 중 최신 1건
            .leftJoin(accountVerification)
            .on(
                accountVerification.id.eq(
                    JPAExpressions
                        .select(avSub.id.max())
                        .from(avSub)
                        .where(
                            avSub.seller.eq(seller),
                            avSub.verified.isTrue()
                        )
                )
            )

            // ✅ pending만
            .where(storeApplication.status.eq(StoreApprovalStatus.PENDING))

            // ✅ 오래된 순
            .orderBy(
                storeApplication.createdAt.asc(),
                storeApplication.id.asc()
            )

            .offset(offset)
            .limit(limit)
            .fetch();
    }
}
