package com.bbangle.bbangle.seller.admin.service;

import static com.bbangle.bbangle.exception.BbangleErrorCode.SELLER_DOCUMENT_NOT_FOUND;
import static com.bbangle.bbangle.exception.BbangleErrorCode.STREAM_CLOSING_ERROR;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.admin.service.model.SellerDocumentDownloadInfo;
import com.bbangle.bbangle.seller.repository.SellerDocumentRepository;
import com.bbangle.bbangle.util.FileNameUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminSellerDocumentService {

    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;
    private static final int READ_TIMEOUT_MILLIS = 10_000;

    private final SellerDocumentRepository sellerDocumentRepository;

    public void downloadSellerDocuments(List<Long> sellerIds, OutputStream outputStream) {
        List<SellerDocumentDownloadInfo> documents =
            sellerDocumentRepository.findDocumentsBySellerIds(sellerIds);

        if (documents.isEmpty()) {
            throw new BbangleException(SELLER_DOCUMENT_NOT_FOUND);
        }

        Set<String> usedEntryPaths = new HashSet<>();
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
            for (SellerDocumentDownloadInfo doc : documents) {
                String entryPath = FileNameUtil.resolveUniquePath(doc.zipEntryPath(), usedEntryPaths);
                usedEntryPaths.add(entryPath);

                zipOut.putNextEntry(new ZipEntry(entryPath));
                URLConnection connection = new URL(doc.url()).openConnection();
                connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
                connection.setReadTimeout(READ_TIMEOUT_MILLIS);
                try (InputStream in = connection.getInputStream()) {
                    in.transferTo(zipOut);
                }
                zipOut.closeEntry();
            }
        } catch (IOException e) {
            throw new BbangleException(STREAM_CLOSING_ERROR, e);
        }
    }
}
