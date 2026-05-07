package com.bbangle.bbangle.store.admin.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerRequest.StoreApplicationApprove;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest.UpdateStoreNameRejectRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameApprove;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameReject;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameRequest;
import com.bbangle.bbangle.store.admin.service.model.RegisterApproveResult;
import com.bbangle.bbangle.store.admin.service.model.UpdateStoreNamesInfo.UpdateStoreNames;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
import com.bbangle.bbangle.store.repository.StoreNameRequestRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStoreService {

    private static final int DEFAULT_PAGE_SIZE = 100;
    private final StoreNameRequestRepository storeNameRequestRepository;
    private final StoreRepository storeRepository;
    private final SellerRepository sellerRepository;
    private final StoreApplicationRepository storeApplicationRepository;

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

    @Transactional
    public UpdateStoreNameApprove approveStoreName(long requestId) {

        StoreNameRequest request = storeNameRequestRepository.findById(requestId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOT_FOUND_REQUEST));

        if (storeRepository.findByStoreName(request.getNewName()).isPresent()) {
            throw new BbangleException(BbangleErrorCode.ALREADY_RESERVED_STORE);
        }

        request.approve();
        Store store = request.getStore();
        return UpdateStoreNameApprove.builder()
            .storeId(store.getId())
            .prevName(request.getCurrentName())
            .updateName(store.getName())
            .status(request.getStatus())
            .modifiedAt(store.getModifiedAt())
            .build();
    }

    @Transactional
    public UpdateStoreNameReject rejectStoreName(long requestId, UpdateStoreNameRejectRequest request) {

        StoreNameRequest storeNameRequest = storeNameRequestRepository.findById(requestId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOT_FOUND_REQUEST));

        storeNameRequest.reject(request.category(), request.rejectDetail());
        return UpdateStoreNameReject.builder()
            .requestId(storeNameRequest.getId())
            .storeId(storeNameRequest.getStore().getId())
            .currentName(storeNameRequest.getCurrentName())
            .newName(storeNameRequest.getNewName())
            .status(storeNameRequest.getStatus())
            .category(storeNameRequest.getRejectCategory())
            .rejectDetail(storeNameRequest.getRejectDetail())
            .build();
    }

    @Transactional
    public Store createStore(StoreApplication storeApplication, String identifier) {

        if (storeRepository.existsByName(storeApplication.getName())) {
            throw new BbangleException(BbangleErrorCode.ALREADY_RESERVED_STORE);
        }

        return storeRepository.save(
            Store.createForSeller(
                storeApplication.getName(),
                storeApplication.getProfile(),
                storeApplication.getIntroduce(),
                identifier,
                storeApplication.getPhoneNumberVO().getPhoneNumber(),
                storeApplication.getPhoneNumberVO().getSubPhoneNumber(),
                storeApplication.getEmailVO().getEmail(),
                storeApplication.getOriginAddressLine(),
                storeApplication.getOriginAddressDetail()
            )
        );
    }

    @Transactional
    public Store updateStore(StoreApplication storeApplication, String identifier) {

        if (sellerRepository.existsByStore_Id(storeApplication.getStore().getId())) {
            throw new BbangleException(BbangleErrorCode.ALREADY_RESERVED_STORE);
        }

        Store store = storeApplication.getStore();
        store.updateStoreForAdmin(
            identifier,
            storeApplication.getProfile(),
            storeApplication.getIntroduce(),
            storeApplication.getPhoneNumberVO().getPhoneNumber(),
            storeApplication.getPhoneNumberVO().getSubPhoneNumber(),
            storeApplication.getEmailVO().getEmail(),
            storeApplication.getOriginAddressLine(),
            storeApplication.getOriginAddressDetail()
        );

        return store;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RegisterApproveResult registerApprove(long applicationId, StoreApplicationApprove command) {

        StoreApplication application = storeApplicationRepository.findByIdWithDetails(applicationId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOT_FOUND_REQUEST));

        Store store;
        if (application.getStore() == null) {
            store = createStore(application, command.identifier());
        } else {
            store = updateStore(application, command.identifier());
        }

        application.getSeller().registerStore(store, command.sellerName());
        application.approve();
        return RegisterApproveResult.builder()
            .storeApplication(application)
            .store(store)
            .seller(application.getSeller())
            .build();
    }
}