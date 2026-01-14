package com.bbangle.bbangle.seller.seller.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.SellerDocument;
import com.bbangle.bbangle.seller.repository.SellerDocumentRepository;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.service.command.RegisterDocumentCommand;
import com.bbangle.bbangle.seller.seller.service.info.SellerDocumentInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerDocumentService {

    private final SellerRepository sellerRepository;
    private final SellerDocumentRepository sellerDocumentRepository;

    @Transactional
    public SellerDocumentInfo registerDocument(RegisterDocumentCommand command) {

        Seller seller = sellerRepository.findById(command.sellerId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND));

        SellerDocument sellerDocument = SellerDocument.create(command.name(), command.url(), command.type(), seller);

        sellerDocumentRepository.save(sellerDocument);

        return SellerDocumentInfo.from(sellerDocument);
    }

}
