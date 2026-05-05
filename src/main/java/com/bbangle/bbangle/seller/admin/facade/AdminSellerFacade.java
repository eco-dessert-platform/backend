package com.bbangle.bbangle.seller.admin.facade;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerRequest.StoreApplicationApprove;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication.SellerDTO;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication.SellerStoreDTO;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationApproveList;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationApproveList.SuccessDetail;
import com.bbangle.bbangle.seller.admin.controller.mapper.AdminStoreMapper;
import com.bbangle.bbangle.seller.admin.service.AdminSellerService;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList;
import com.bbangle.bbangle.store.admin.service.AdminStoreApplicationService;
import com.bbangle.bbangle.store.admin.service.AdminStoreService;
import com.bbangle.bbangle.store.admin.service.model.RegisterApproveResult;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.util.AesEncryptionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminSellerFacade {

    private final AdminSellerService adminSellerService;
    private final AesEncryptionUtil aesEncryptionUtil;
    private final AdminStoreApplicationService adminStoreApplicationService;
    private final AdminStoreService adminStoreService;
    private final AdminStoreMapper adminStoreMapper;

    public AdminSellerResponse.AdminSellerApplicationList getAdminSellerApplicationList(int page) {

        SellerApplicationInfoList raw = adminSellerService.getAdminSellerApplicationList(page);

        List<AdminSellerApplication> content = raw.sellerApplicationInfoList().stream()
            .map(this::toResponse)
            .toList();

        return AdminSellerResponse.AdminSellerApplicationList.builder()
            .adminSellerApplicationList(content)
            .totalElements(raw.totalElements())
            .totalPages(raw.totalPages())
            .hasPrevious(raw.hasPrevious())
            .hasNext(raw.hasNext())
            .build();
    }

    private AdminSellerResponse.AdminSellerApplication toResponse(
        AdminSellerInfo.SellerApplicationInfoList.SellerApplicationInfo raw
    ) {
        String decryptedAccountNumber = aesEncryptionUtil.decrypt(raw.sellerInfo().accountNumber());

        return AdminSellerApplication.builder()
            .storeApplicationId(raw.storeApplicationId())
            .sellerStoreDTO(
                SellerStoreDTO.builder()
                    .storeName(raw.sellerStoreInfo().storeName())
                    .phone(raw.sellerStoreInfo().phone())
                    .subPhone(raw.sellerStoreInfo().subPhone())
                    .email(raw.sellerStoreInfo().email())
                    .originAddressLine(raw.sellerStoreInfo().originAddressLine())
                    .originAddressDetail(raw.sellerStoreInfo().originAddressDetail())
                    .build()
            )
            .sellerDTO(
                SellerDTO.builder()
                    .sellerId(raw.sellerInfo().sellerId())
                    .bankCode(raw.sellerInfo().bankCode())
                    .accountHolder(raw.sellerInfo().accountHolder())
                    .accountNumber(decryptedAccountNumber)
                    .createdAt(raw.sellerInfo().createdAt())
                    .build()
            )
            .build();
    }

    // TODO : Test
    public AdminSellerApplicationApproveList approveStoreApplications(List<StoreApplicationApprove> requests) {

        Map<Long, StoreApplicationApprove> commandMap = requests.stream()
            .collect(Collectors.toMap(StoreApplicationApprove::applicationId, Function.identity()));
        List<Long> ids = requests.stream().map(StoreApplicationApprove::applicationId).toList();

        List<AdminSellerApplicationApproveList.SuccessDetail> successDetails = new ArrayList<>();
        List<AdminSellerResponse.FailDetail> failDetails = new ArrayList<>();

        List<StoreApplication> storeApplications = adminStoreApplicationService.findAllByIds(ids);
        Map<Long, StoreApplication> map = storeApplications.stream()
            .collect(Collectors.toMap(StoreApplication::getId, Function.identity()));

        for (Long id : ids) {
            StoreApplication storeApplication = map.get(id);
            StoreApplicationApprove command = commandMap.get(id);

            if (storeApplication == null) {
                failDetails.add(
                    AdminSellerResponse.FailDetail.builder()
                    .storeApplicationId(id)
                    .reason(BbangleErrorCode.NOT_FOUND_REQUEST.getMessage())
                    .build()
                );
                continue;
            }

            try {
                storeApplication.validateApprovable();
                RegisterApproveResult result = adminStoreService.registerApprove(storeApplication.getId(), command);

                successDetails.add(
                    SuccessDetail.builder()
                        .storeApplicationId(result.storeApplication().getId())
                        .storeApplicationStatus(result.storeApplication().getStatus())
                        .storeDTO(adminStoreMapper.toApproveStoreDto(result.store()))
                        .sellerDTO(adminStoreMapper.toApproveSellerDto(result.seller()))
                        .build()
                );
            } catch (DataIntegrityViolationException e) {
                log.warn("UNIQUE constraint violation - Approve storeApplicationId={}", id, e);
                failDetails.add(
                    AdminSellerResponse.FailDetail.builder()
                        .storeApplicationId(id)
                        .reason(BbangleErrorCode.ALREADY_RESERVED_STORE.getMessage())
                        .build()
                );
            } catch (BbangleException e) {
                failDetails.add(
                    AdminSellerResponse.FailDetail.builder()
                        .storeApplicationId(id)
                        .reason(e.getMessage())
                        .build()
                );
            }
        }

        return AdminSellerApplicationApproveList.builder()
            .successDetails(successDetails)
            .failDetails(failDetails)
            .build();
    }
}
