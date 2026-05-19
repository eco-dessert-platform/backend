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
    /** 스토어명 검색 결과 한 페이지당 표시 건수 */
    private static final int SEARCH_PAGE_SIZE = 20;
    private final StoreNameRequestRepository storeNameRequestRepository;
    private final StoreRepository storeRepository;
    private final SellerRepository sellerRepository;
    private final StoreApplicationRepository storeApplicationRepository;
    private final AdminStoreMapper adminStoreMapper;

    @Transactional
    public StoreDetailResponse updateStoreWithName(long storeId, StoreDetailRequest request) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new BbangleException(BbangleErrorCode.STORE_NOT_FOUND));

        if (!store.getName().equals(request.storeName()) && storeRepository.existsByName(request.storeName())) {
            throw new BbangleException(BbangleErrorCode.INVALID_STORE_NAME);
        }

        store.updateStoreWithName(
            request.storeName(),
            request.identifier(),
            request.introduce(),
            request.phoneNumber(),
            request.subPhoneNumber(),
            request.email(),
            request.originAddress(),
            request.originAddressDetail()
        );

        return adminStoreMapper.toStoreDetailResponse(store);
    }
    private final AdminStoreMapper adminStoreMapper;

    @Transactional
    public StoreDetailResponse updateStoreWithName(long storeId, StoreDetailRequest request) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new BbangleException(BbangleErrorCode.STORE_NOT_FOUND));

        if (!store.getName().equals(request.storeName()) && storeRepository.existsByName(request.storeName())) {
            throw new BbangleException(BbangleErrorCode.INVALID_STORE_NAME);
        }

        store.updateStoreWithName(
            request.storeName(),
            request.identifier(),
            request.introduce(),
            request.phoneNumber(),
            request.subPhoneNumber(),
            request.email(),
            request.originAddress(),
            request.originAddressDetail()
        );

        return adminStoreMapper.toStoreDetailResponse(store);
    }

    /**
     * 관리자 스토어명 검색 — page 기반 페이지네이션 (페이지 크기 20 고정, id 오름차순)
     * storeName 이 null 또는 빈 문자열이면 활성 스토어 전체를 반환한다.
     */
    @Transactional(readOnly = true)
    public StoreSearchResult searchStoresByName(String storeName, int page) {
        page = Math.max(page, 1);
        // 서비스 단에서 공백 제거 정규화 (DB 컬럼의 공백도 REPLACE로 제거하여 비교)
        String normalized = (storeName == null) ? "" : storeName.replaceAll("\\s+", "");
        Pageable pageable = PageRequest.of(page - 1, SEARCH_PAGE_SIZE);
        Page<Store> result = storeRepository.findActiveStoresByName(normalized, pageable);

        return StoreSearchResult.builder()
            .storeSummaries(result.getContent().stream().map(StoreSummary::from).toList())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .hasPrevious(result.hasPrevious())
            .hasNext(result.hasNext())
            .build();
    }

    @Transactional(readOnly = true)
    public Page<RegisteredStoreInfo> getRegisteredStores(Pageable pageable) {
        return storeRepository.findAll(pageable)
            .map(RegisteredStoreInfo::from);
    }

    @Transactional
    public void deleteStores(List<Long> storeIds) {
        long count = storeRepository.countByIdIn(storeIds);
        if (count != storeIds.size()) {
            throw new BbangleException(BbangleErrorCode.STORE_NOT_FOUND);
        }
        sellerRepository.clearStoreAndResetStatusByStoreIdIn(storeIds, CertificationStatus.NEW);
        storeRepository.deleteAllByIdInBatch(storeIds);
    }

    @Transactional(readOnly = true)
    public UpdateStoreNameRequest getPendingRequests(int page) {
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
    public Store createStore(AdminStoreInfo adminStoreInfo) {

        if (storeRepository.existsByName(adminStoreInfo.storeName())) {
            throw new BbangleException(BbangleErrorCode.ALREADY_RESERVED_STORE);
        }

        return storeRepository.save(
            Store.createForSeller(
                adminStoreInfo.storeName(),
                adminStoreInfo.profile(),
                adminStoreInfo.introduce(),
                adminStoreInfo.identifier(),
                adminStoreInfo.phoneNumber(),
                adminStoreInfo.subPhoneNumber(),
                adminStoreInfo.email(),
                adminStoreInfo.address(),
                adminStoreInfo.addressDetail()
            )
        );
    }

    @Transactional
    public Store updateStore(AdminStoreInfo adminStoreInfo, Store store) {

        store.updateStoreForAdmin(
            adminStoreInfo.identifier(),
            adminStoreInfo.profile(),
            adminStoreInfo.introduce(),
            adminStoreInfo.phoneNumber(),
            adminStoreInfo.subPhoneNumber(),
            adminStoreInfo.email(),
            adminStoreInfo.address(),
            adminStoreInfo.addressDetail()
        );

        return store;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RegisterApproveResult registerApprove(long applicationId, StoreApplicationApprove command) {

        StoreApplication application = storeApplicationRepository.findByIdWithDetails(applicationId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.NOT_FOUND_REQUEST));
        application.validateApprovable();

        AdminStoreInfo adminStoreInfo = AdminStoreInfo.builder()
            .storeName(application.getName())
            .profile(application.getProfile())
            .introduce(application.getIntroduce())
            .identifier(command.identifier())
            .phoneNumber(application.getPhoneNumberVO().getPhoneNumber())
            .subPhoneNumber(application.getPhoneNumberVO().getSubPhoneNumber())
            .email(application.getEmailVO().getEmail())
            .address(application.getOriginAddressLine())
            .addressDetail(application.getOriginAddressDetail())
            .build();

        Store store;
        if (application.getStore() == null) {
            store = createStore(adminStoreInfo);
        } else {
            if (sellerRepository.existsByStore_Id(application.getStore().getId())) {
                throw new BbangleException(BbangleErrorCode.ALREADY_RESERVED_STORE);
            }

            store = updateStore(adminStoreInfo, application.getStore());
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