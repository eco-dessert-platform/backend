package com.bbangle.bbangle.store.seller.controller.mapper;

import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.seller.controller.dto.StoreApplicationResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface SellerStoreApplicationMapper {

    @Mapping(source = "id", target = "storeApplicationId")
    @Mapping(source = "seller.id", target = "sellerId")
    @Mapping(source = "store.id", target = "storeId")
    @Mapping(source = "phoneNumberVO.phoneNumber", target = "phoneNumber")
    @Mapping(source = "phoneNumberVO.subPhoneNumber", target = "subPhoneNumber")
    @Mapping(source = "emailVO.email", target = "email")
    @Mapping(source = "originAddressLine", target = "originAddress")
    StoreApplicationResponse.StoreApplicationDetail toStoreApplicationDetail(StoreApplication storeApplication);
}
