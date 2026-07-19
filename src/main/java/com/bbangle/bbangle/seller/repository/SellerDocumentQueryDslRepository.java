package com.bbangle.bbangle.seller.repository;

import com.bbangle.bbangle.seller.admin.service.model.SellerDocumentDownloadInfo;
import java.util.List;

public interface SellerDocumentQueryDslRepository {

    List<SellerDocumentDownloadInfo> findDocumentsBySellerIds(List<Long> sellerIds);
}
