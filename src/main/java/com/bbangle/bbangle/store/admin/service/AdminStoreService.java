package com.bbangle.bbangle.store.admin.service;

import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.admin.service.model.UpdateStoreNamesInfo.UpdateStoreNames;
import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.repository.StoreNameRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStoreService {

    private static final int DEFAULT_PAGE_SIZE = 100;
    private final StoreNameRequestRepository storeNameRequestRepository;

    @Transactional(readOnly = true)
    public AdminStoreResponse.UpdateStoreNameRequest getPendingRequests(int page) {
        page = Math.max(page, 1);
        Pageable pageable = PageRequest.of(
            page - 1,
            DEFAULT_PAGE_SIZE,
            Sort.by(
                Sort.Order.asc("createdAt"),
                Sort.Order.asc("id")
            )
        );

        Page<StoreNameRequest> results = storeNameRequestRepository.findByStatus(StoreApprovalStatus.PENDING, pageable);

        return UpdateStoreNameRequest.builder()
            .updateStoreNames(
                results.getContent()
                    .stream()
                    .map(UpdateStoreNames::from)
                    .toList()
            )
            .totalElements(results.getTotalElements())
            .totalPages(results.getTotalPages())
            .hasPrevious(results.hasPrevious())
            .hasNext(results.hasNext())
            .build();
    }
}