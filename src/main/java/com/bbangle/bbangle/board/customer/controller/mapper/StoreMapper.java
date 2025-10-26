package com.bbangle.bbangle.board.customer.controller.mapper;

import com.bbangle.bbangle.board.customer.service.dto.StoreInfo;
import com.bbangle.bbangle.store.controller.dto.StoreResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface StoreMapper {

    StoreResponse.StoreDetail toStoreDetailResponse(StoreInfo.StoreDetail storeDetail);

}
