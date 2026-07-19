package com.bbangle.bbangle.store.admin.controller.mapper;

import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest.StoreDetailRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.StoreDetailResponse;
import com.bbangle.bbangle.store.admin.service.model.AdminStoreInfo;
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
public interface AdminStoreMapper {

    @Mapping(source = "request.storeName", target = "storeName")
    @Mapping(source = "profileImagePath", target = "profile")
    @Mapping(source = "request.introduce", target = "introduce")
    @Mapping(source = "request.identifier", target = "identifier")
    @Mapping(source = "request.phoneNumber", target = "phoneNumber")
    @Mapping(source = "request.subPhoneNumber", target = "subPhoneNumber")
    @Mapping(source = "request.email", target = "email")
    @Mapping(source = "request.originAddress", target = "address")
    @Mapping(source = "request.originAddressDetail", target = "addressDetail")
    AdminStoreInfo toAdminStoreInfo(StoreDetailRequest request, String profileImagePath);

    @Mapping(source = "id", target = "storeId")
    @Mapping(source = "phoneNumberVO.phoneNumber", target = "phoneNumber")
    @Mapping(source = "phoneNumberVO.subPhoneNumber", target = "subPhoneNumber")
    @Mapping(source = "emailVO.email", target = "email")
    @Mapping(source = "originAddressLine", target = "originAddress")
    StoreDetailResponse toStoreDetailResponse(Store store);
}
