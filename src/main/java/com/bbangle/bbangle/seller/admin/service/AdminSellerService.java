package com.bbangle.bbangle.seller.admin.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationRejectList;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationRejectList.FailDetail;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList.SellerApplicationInfo;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
        long totalElements = storeApplicationRepository.countSellerApplications();
        int totalPages = (int) Math.ceil((double) totalElements / DEFAULT_PAGE_SIZE);

        return SellerApplicationInfoList.builder()
            .sellerApplicationInfoList(content)
            .totalPages(totalPages)
            .totalElements(totalElements)
            .hasPrevious(page > 1)
            .hasNext(page < totalPages)
            .build();
    }

    @Transactional
    public AdminSellerResponse.AdminSellerApplicationRejectList rejectStoreApplications(List<Long> ids) {

        List<Long> successIds = new ArrayList<>();
        List<AdminSellerApplicationRejectList.FailDetail> failDetails = new ArrayList<>();

        List<StoreApplication> applications = storeApplicationRepository.findAllWithSellerByIdIn(ids);

        Map<Long, StoreApplication> map = applications.stream()
            .collect(Collectors.toMap(StoreApplication::getId, Function.identity()));

        for (Long id : ids) {
            StoreApplication app = map.get(id);

            if (app == null) {
                failDetails.add(FailDetail.builder()
                    .storeApplicationId(id)
                    .reason(BbangleErrorCode.NOT_FOUND_REQUEST.getMessage())
                    .build()
                );
                continue;
            }

            try {
                app.reject();
                successIds.add(id);
            } catch (BbangleException e) {
                failDetails.add(FailDetail.builder()
                    .storeApplicationId(id)
                    .reason(e.getMessage())
                    .build()
                );
            } catch (Exception e) {
                failDetails.add(FailDetail.builder()
                    .storeApplicationId(id)
                    .reason("알 수 없는 오류")
                    .build()
                );
            }
        }

        return AdminSellerApplicationRejectList.builder()
            .successIds(successIds)
            .failDetails(failDetails)
            .build();
    }
}
