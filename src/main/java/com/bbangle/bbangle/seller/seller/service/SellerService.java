package com.bbangle.bbangle.seller.seller.service;

import static com.bbangle.bbangle.seller.domain.model.DocumentType.BANKBOOK_COPY;
import static com.bbangle.bbangle.seller.domain.model.DocumentType.BUSINESS_REGISTRATION_CERTIFICATE;
import static com.bbangle.bbangle.seller.domain.model.DocumentType.INSTANT_FOOD_MANUFACTURING_PROCESSING_REGISTRATION;
import static com.bbangle.bbangle.seller.domain.model.DocumentType.MAIL_ORDER_SALES_REPORT;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.SellerDocument;
import com.bbangle.bbangle.seller.domain.model.DocumentType;
import com.bbangle.bbangle.seller.repository.SellerDocumentRepository;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerAccountUpdateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerDocumentsRegisterRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerStoreNameUpdateRequest;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerRequest.SellerUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Transactional
@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final SellerDocumentRepository sellerDocumentRepository;
    private final S3Service s3Service;

    public void updateSeller(SellerUpdateRequest request, Long sellerId) {
        // TODO: 실제 비즈니스 로직 구현
    }

    public void updateStoreName(SellerStoreNameUpdateRequest request, Long sellerId) {

        // TODO: 실제 비즈니스 로직 구현

    }

    public void updateAccount(SellerAccountUpdateRequest request, Long sellerId) {

        // TODO: 실제 비즈니스 로직 구현

    }

    public void registerDocuments(Long memberId, SellerDocumentsRegisterRequest request) {
        Seller seller = findSellerByMemberId(memberId);

        saveFileAndUpdateOrInsertDocument(seller, BUSINESS_REGISTRATION_CERTIFICATE, request.businessLicense());
        saveFileAndUpdateOrInsertDocument(seller, MAIL_ORDER_SALES_REPORT, request.mailOrderLicense());
        saveFileAndUpdateOrInsertDocument(seller, BANKBOOK_COPY, request.bankbookCopy());
        saveFileAndUpdateOrInsertDocument(seller, INSTANT_FOOD_MANUFACTURING_PROCESSING_REGISTRATION, request.foodManufactureLicense());
    }

    private void saveFileAndUpdateOrInsertDocument(Seller seller, DocumentType type, MultipartFile file) {
        String s3Path = s3Service.saveImage(file, type.getFolderName());

        SellerDocument document = sellerDocumentRepository.findBySellerAndType(seller, type)
            .orElseGet(() -> SellerDocument.register(seller, type, s3Path));

        document.updateUrl(s3Path);
        sellerDocumentRepository.save(document);
    }

    private Seller findSellerByMemberId(Long memberId) {
        return sellerRepository.findByMember_id(memberId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOT_FOUND_SELLER));
    }

}
