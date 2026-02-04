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
    StoreResponse.SellerStoreDetail toSellerStoreDetail(Store store);
}
