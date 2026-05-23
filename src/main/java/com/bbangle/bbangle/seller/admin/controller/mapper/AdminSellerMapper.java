package com.bbangle.bbangle.seller.admin.controller.mapper;

import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationApproveList;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.Store;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface AdminSellerMapper {

    @Mapping(source = "id", target = "storeId")
    @Mapping(source = "name", target = "storeName")
    @Mapping(source = "phoneNumberVO.phoneNumber", target = "phone")
    @Mapping(source = "phoneNumberVO.subPhoneNumber", target = "subPhone")
    @Mapping(source = "emailVO.email", target = "email")
    AdminSellerApplicationApproveList.SuccessDetail.StoreDTO toApproveStoreDto(Store store);

    @Mapping(source = "id", target = "sellerId")
    @Mapping(source = "name", target = "sellerName")
    @Mapping(source = "certificationStatus", target = "sellerStatus")
    AdminSellerApplicationApproveList.SuccessDetail.SellerDTO toApproveSellerDto(Seller seller);
}
