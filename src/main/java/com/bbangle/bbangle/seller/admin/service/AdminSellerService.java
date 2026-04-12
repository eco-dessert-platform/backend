package com.bbangle.bbangle.seller.admin.service;

import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList.SellerApplicationInfo;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminSellerService {

    private static final int DEFAULT_PAGE_SIZE = 100;
    private final StoreApplicationRepository storeApplicationRepository;

    @Transactional(readOnly = true)
    public SellerApplicationInfoList getAdminSellerApplicationList(int page) {
        int offset = (page - 1) * DEFAULT_PAGE_SIZE;

        List<SellerApplicationInfo> content = storeApplicationRepository.findSellerApplications(offset, DEFAULT_PAGE_SIZE);
        long totalElements = storeApplicationRepository.countByStatus(StoreApprovalStatus.PENDING);
        int totalPages = (int) Math.ceil((double) totalElements / DEFAULT_PAGE_SIZE);

        return SellerApplicationInfoList.builder()
            .sellerApplicationInfoList(content)
            .totalPages(totalPages)
            .totalElements(totalElements)
            .hasPrevious(page > 1)
            .hasNext(page < totalPages)
            .build();
    }
}
