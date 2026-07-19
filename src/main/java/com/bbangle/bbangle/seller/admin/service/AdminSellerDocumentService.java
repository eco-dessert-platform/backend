package com.bbangle.bbangle.seller.admin.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.SELLER_DOCUMENT_NOT_FOUND;
import static com.bbangle.bbangle.exception.BbangleErrorCode.STREAM_CLOSING_ERROR;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.admin.service.model.SellerDocumentDownloadInfo;
import com.bbangle.bbangle.seller.repository.SellerDocumentRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminSellerDocumentService {

    private final SellerDocumentRepository sellerDocumentRepository;

    public void downloadSellerDocuments(List<Long> sellerIds, OutputStream outputStream) {
        List<SellerDocumentDownloadInfo> documents =
            sellerDocumentRepository.findDocumentsBySellerIds(sellerIds);

        if (documents.isEmpty()) {
            throw new BbangleException(SELLER_DOCUMENT_NOT_FOUND);
        }

        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
            for (SellerDocumentDownloadInfo doc : documents) {
                zipOut.putNextEntry(new ZipEntry(doc.zipEntryPath()));
                try (InputStream in = new URL(doc.url()).openStream()) {
                    in.transferTo(zipOut);
                }
                zipOut.closeEntry();
            }
        } catch (IOException e) {
            throw new BbangleException(STREAM_CLOSING_ERROR, e);
        }
    }
}
