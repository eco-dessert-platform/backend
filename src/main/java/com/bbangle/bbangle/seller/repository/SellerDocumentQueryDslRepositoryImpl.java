package com.bbangle.bbangle.seller.repository;

import static com.bbangle.bbangle.seller.domain.QSeller.seller;
import static com.bbangle.bbangle.seller.domain.QSellerDocument.sellerDocument;
import static com.bbangle.bbangle.store.domain.QStore.store;

import com.bbangle.bbangle.seller.admin.service.model.SellerDocumentDownloadInfo;
import com.bbangle.bbangle.seller.domain.model.DocumentType;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SellerDocumentQueryDslRepositoryImpl implements SellerDocumentQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SellerDocumentDownloadInfo> findDocumentsBySellerIds(List<Long> sellerIds) {
        return queryFactory
            .select(Projections.constructor(
                SellerDocumentDownloadInfo.class,
                store.name,
                sellerDocument.url,
                sellerDocument.type,
                sellerDocument.name
            ))
            .from(sellerDocument)
            .join(sellerDocument.seller, seller)
            .join(seller.store, store)
            .where(seller.id.in(sellerIds))
            .fetch();
    }
}
