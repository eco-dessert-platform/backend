package com.bbangle.bbangle.seller.repository;

import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.SellerDocument;
import com.bbangle.bbangle.seller.domain.model.DocumentType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerDocumentRepository extends JpaRepository<SellerDocument, Long> {
    Optional<SellerDocument> findBySellerAndType(Seller seller, DocumentType type);
}
