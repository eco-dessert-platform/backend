package com.bbangle.bbangle.store.seller.controller.mapper;

import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface SellerStoreMapper {

    @Mapping(source = "id", target = "storeId")
    @Mapping(source = "phoneNumberVO.phoneNumber", target = "phoneNumber")
    @Mapping(source = "phoneNumberVO.subPhoneNumber", target = "subPhoneNumber")
    @Mapping(source = "emailVO.email", target = "email")
    @Mapping(source = "originAddressLine", target = "originAddress")
    @Mapping(source = "originAddressDetail", target = "originAddressDetail")
    StoreResponse.SellerStoreDetail toSellerStoreDetail(Store store);
}
