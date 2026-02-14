package com.bbangle.bbangle.seller.seller.facade;

import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.seller.controller.dto.SellerResponse;
import com.bbangle.bbangle.seller.seller.facade.command.RegisterDocumentsCommand;
import com.bbangle.bbangle.seller.seller.facade.dto.DocumentUploadInfo;
import com.bbangle.bbangle.seller.seller.facade.dto.UploadedDocument;
import com.bbangle.bbangle.seller.seller.service.AccountVerificationService;
import com.bbangle.bbangle.seller.seller.service.SellerDocumentService;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.seller.seller.service.command.RegisterDocumentCommand;
import com.bbangle.bbangle.seller.seller.service.info.SellerDocumentInfo;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.seller.controller.mapper.SellerStoreMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerFacade {

    private static final String SELLER_DOCUMENT_FOLDER = "seller-documents";
    private final S3Service s3Service;
    private final SellerDocumentService sellerDocumentService;
    private final AccountVerificationService accountVerificationService;
    private final SellerService sellerService;
    private final SellerStoreMapper sellerStoreMapper;

    @Transactional
    public List<SellerDocumentInfo> registerDocuments(RegisterDocumentsCommand command) {

        List<SellerDocumentInfo> sellerDocumentInfos = new ArrayList<>();
        // 1. 계좌 인증 확인
        accountVerificationService.confirmAccount(command.accountVerificationId());

        // 2. 문서 정보 매핑 (타입과 파일을 쌍으로 관리)
        List<DocumentUploadInfo> documents = List.of(
            new DocumentUploadInfo("BUSINESS_REGISTRATION_CERTIFICATE", command.businessLicense()),
            new DocumentUploadInfo("MAIL_ORDER_SALES_REPORT", command.mailOrderLicense()),
            new DocumentUploadInfo("BANKBOOK_COPY", command.bankbookCopy()),
            new DocumentUploadInfo("INSTANT_FOOD_MANUFACTURING_PROCESSING_REGISTRATION",
                command.foodManufactureLicense())
        );

        // 3. S3 업로드 및 경로 저장
        List<UploadedDocument> uploadedDocuments = new ArrayList<>();
        try {
            for (DocumentUploadInfo doc : documents) {
                String filePath = s3Service.saveAndReturnWithCdn(SELLER_DOCUMENT_FOLDER, doc.file());
                String fileName = doc.file().getOriginalFilename();
                uploadedDocuments.add(new UploadedDocument(doc.type(), filePath, fileName));
            }

            // 4. DB 저장
            for (UploadedDocument uploaded : uploadedDocuments) {
                SellerDocumentInfo sellerDocumentInfo = sellerDocumentService.registerDocument(
                    new RegisterDocumentCommand(
                        command.sellerId(),
                        uploaded.filePath(),
                        uploaded.fileName(),
                        uploaded.type()
                    )
                );
                sellerDocumentInfos.add(sellerDocumentInfo);
            }
            return sellerDocumentInfos;
        } catch (Exception e) {
            // 5. 롤백: 업로드된 모든 파일 삭제
            uploadedDocuments.forEach(doc -> {
                log.error("Document 생성 실패로 인한 S3 문서 롤백: {}", doc.filePath());
                s3Service.deleteImage(doc.filePath());
            });
            throw e;
        }
    }

    public SellerResponse.RegisteredStoreDetail getRegisteredStoreDetail(Long sellerId) {
        Seller seller = sellerService.getSellerById(sellerId);
        Store store = seller.getStore();

        if (store == null) {
            return SellerResponse.RegisteredStoreDetail.builder()
                .sellerId(seller.getId())
                .store(null)
                .build();
        } else {
            return SellerResponse.RegisteredStoreDetail.builder()
                .sellerId(seller.getId())
                .store(sellerStoreMapper.toSellerStoreDetail(store))
                .build();
        }
    }
}
